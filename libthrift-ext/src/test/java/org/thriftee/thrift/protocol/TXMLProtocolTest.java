/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.thrift.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.thriftee.examples.Examples.everythingStruct;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;
import org.junit.Test;
import org.xml.sax.SAXException;

import everything.EndOfTheUniverseException;
import everything.Everything;
import everything.Universe;
import everything.UniverseImpl;

public class TXMLProtocolTest {

  public void validate(TXMLProtocol protocol, String msg) throws SAXException, IOException {
    System.out.print("Validating against " + protocol.schemaUrl() + " ...");
    final String validationError = protocol.validate(msg);
    if (validationError != null) {
      System.out.println(" invalid!\n" + validationError + "\n");
      fail(validationError);
    }
    System.out.println(" valid.\n\n");
  }

  @Test
  public void testEverything() throws Exception {
    final Everything struct = everythingStruct();
    testRoundtrip(Everything.class, struct);
  }

  @Test
  public void testControlChars() throws Exception {
    final Everything struct = everythingStruct();
    struct.str = "some control chars: \1 (?\2??)";
//    struct.str_list.add("tester\1");
    testRoundtrip(Everything.class, struct);
  }

  @Test
  public void testElementToByte() throws Exception {
    for (final Field field : TType.class.getDeclaredFields()) {
      final String name = field.getName();
      final byte value = field.getByte(null);
      final String element = TXMLProtocol.byteToElement(value);
      final byte roundtrip = TXMLProtocol.elementToByte(element);
      System.out.printf("%10s = %2s, %2s, %2s%n", name, value, element, roundtrip);
      assertEquals("roundtrip should be equals for type " + name, value, roundtrip);
    }
  }

  @Test
  public void testMessageTypeToByte() throws Exception {
    for (final Field field : TMessageType.class.getDeclaredFields()) {
      final String name = field.getName();
      final byte value = field.getByte(null);
      final String element = TXMLProtocol.byteToMessageType(value);
      final byte roundtrip = TXMLProtocol.messageTypeToByte(element);
      System.out.printf("%10s = %2s, %2s, %2s%n", name, value, element, roundtrip);
      assertEquals("roundtrip should be equals for type " + name, value, roundtrip);
    }
  }

  @Test
  public void testService() throws Exception {

    final Everything o = everythingStruct();

    final TestProtocol protocol = createOutProtocol(null);
    Universe.Client client = new Universe.Client.Factory().getClient(protocol);
    client.send_grok(o);

    final String serialized = protocol.getFormattedXmlOutput();
    System.out.println("Request:\n-----------------------\n" + serialized);
    validate(protocol, serialized);

    final TestProtocol protocol2 = createOutProtocol(serialized);
    TProcessor processor = new Universe.Processor<>(new UniverseImpl());
    processor.process(protocol2, protocol2);

    final String response = protocol2.getFormattedXmlOutput();
    System.out.println("Response:\n-----------------------\n" + response);
    validate(protocol, response);

    final TProtocol protocol3 = createOutProtocol(response);

    TMessage rmsg = protocol3.readMessageBegin();
    assertEquals(TMessageType.REPLY, rmsg.type);

    protocol3.readStructBegin();

    TField rfield = protocol3.readFieldBegin();
    assertEquals(TType.I32, rfield.type);

    int answer = protocol3.readI32();
    assertEquals(42, answer);
    protocol3.readFieldEnd();

    TField stop = protocol3.readFieldBegin();
    assertEquals(TType.STOP, stop.type);

    protocol3.readStructEnd();
    protocol3.readMessageEnd();

  }

  @Test
  public void testException() throws Exception {

    final Everything o = everythingStruct();

    final TestProtocol protocol = createOutProtocol(null);
    Universe.Client client = new Universe.Client.Factory().getClient(protocol);
    client.send_grok(o);

    final String serialized = protocol.getFormattedXmlOutput();
    System.out.println("Request:\n-----------------------\n" + serialized);
    validate(protocol, serialized);

    final TestProtocol protocol2 = createOutProtocol(serialized);
    TProcessor processor = new Universe.Processor<>(new UniverseImpl() {
      @Override
      public int grok(Everything arg0) throws TException {
        throw new EndOfTheUniverseException("its over!!");
      }
    });
    processor.process(protocol2, protocol2);

    final String response = protocol2.getFormattedXmlOutput();
    System.out.println("Response:\n-----------------------\n" + response);
    validate(protocol, response);

  }

  public <T extends TBase<?, ?>> void testRoundtrip(Class<T> cl, T o)
          throws Exception {

    TestProtocol protocol = createOutProtocol(null);
    o.write(protocol);

    final String serialized = protocol.getFormattedXmlOutput();
    System.out.println("Serialized:\n-----------------------\n" + serialized);
    validate(protocol, serialized);

    protocol = createOutProtocol(serialized);
    T roundtrip = cl.newInstance();
    roundtrip.read(protocol);
    assertEquals(
      "object read from serialized form should equal input object",
      o, roundtrip
    );

    roundtrip.write(protocol);
    final String rounded = protocol.getFormattedXmlOutput();
    System.out.println("Round Trip:\n-----------------------\n" + rounded);
    validate(protocol, rounded);

    //assertEquals(serialized, rounded);

  }

  public TestProtocol createOutProtocol(String s) {
    return new TestProtocol(s);
  }

}
