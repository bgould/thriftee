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
package org.thriftee.thrift.xml.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

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

import another.Blotto;
import everything.EndOfTheUniverseException;
import everything.Everything;
import everything.Spinkle;
import everything.Spirfle;
import everything.Sprat;
import everything.Universe;

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

    final Everything o = TXMLProtocolTest.everythingStruct();

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

  public static Everything everythingStruct() {
    Everything everything = new Everything();
    everything.bite = 42;
    everything.int32 = 64000;
    everything.int16 = 1024;
    everything.int64 = 10000000000L;
    everything.str = "foobar";
    everything.dbl = 10.4;
    everything.bin = ByteBuffer.wrap("secret_password".getBytes());
    everything.onion = Sprat.wowzer(1337);
    everything.setReally(true);

    final Map<String, String> str_str_map = new HashMap<String, String>();
    str_str_map.put("foo", "bar");
    str_str_map.put("graffle", "florp");
    everything.str_str_map = str_str_map;

    final List<String> str_list = new ArrayList<String>();
    str_list.add("wibble");
    str_list.add("snork");
    str_list.add("spiffle");
    everything.str_list = str_list;

    final List<Spinkle> enum_list = new ArrayList<Spinkle>();
    enum_list.add(Spinkle.HRRR);
    enum_list.add(Spinkle.REWT);
    everything.enum_list = enum_list;

    final List<Spirfle> obj_list = new ArrayList<Spirfle>();
    obj_list.add(new Spirfle("blat", 17, Spinkle.HRRR, 1, null, null));
    obj_list.add(new Spirfle("yarp", 89, Spinkle.REWT, 2, null, null));
    obj_list.add(new Spirfle("trop", 9, null, 3, null, null));
    everything.obj_list = obj_list;

    final Map<Integer, Spirfle> int_obj_map = new LinkedHashMap<>();
    for (int i = 0, c = obj_list.size(); i < c; i++) {
      int_obj_map.put(i + 1, obj_list.get(i));
    }
    everything.int_obj_map = int_obj_map;

    everything.obj = obj_list.get(0);
    everything.obj_set = new LinkedHashSet<>(obj_list);
    everything.str_set = new LinkedHashSet<>(str_list);

    final List<List<Integer>> int_list_list = new ArrayList<>();
    int_list_list.add(Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 }));
    int_list_list.add(Arrays.asList(new Integer[] { 1, 1, 3, 5 }));
    everything.int_list_list = int_list_list;

    everything.smork = new Blotto(42, "happelsmack");

    Map<Spinkle, List<Spirfle>> enum_list_map = new HashMap<>();
    List<Spirfle> spirfles = new ArrayList<>();
    spirfles.add(new Spirfle("fink", 2, null, 34, null, null));
    enum_list_map.put(Spinkle.HRRR, spirfles);
    everything.enum_list_map = enum_list_map;

    everything.empty = "";

    return everything;
  }

}
