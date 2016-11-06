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
package org.thriftee.core.parser;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.core.parser.ThriftParser;
import org.thriftee.core.parser.ThriftParserHandler;
import org.thriftee.core.parser.ThriftParserHandlerChain;
import org.thriftee.core.proxy.TProtocolProxy;
import org.thriftee.core.tests.AbstractThriftEETest;
import org.thriftee.examples.Examples;
import org.thriftee.thrift.protocol.TXMLProtocol;
import org.thriftee.thrift.schema.AbstractFieldSchema;
import org.thriftee.thrift.schema.AbstractStructSchema;
import org.thriftee.thrift.schema.EnumSchema;
import org.thriftee.thrift.schema.MethodSchema;
import org.thriftee.thrift.schema.ServiceSchema;
import org.thriftee.thrift.schema.StructSchema;

import everything.Everything;
import everything.Universe;

@RunWith(Parameterized.class)
public class ThriftParserTest extends AbstractThriftEETest {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      new Object[] { new TBinaryProtocol.Factory() },
      new Object[] { new TCompactProtocol.Factory() },
      new Object[] { new TJSONProtocol.Factory() },
      new Object[] { new TXMLProtocol.Factory() },
    });
  }

  @Override
  protected boolean generateClients() {
    return false;
  }

  private final TProtocolFactory fctry;

  public ThriftParserTest(TProtocolFactory fctry) {
    this.fctry = fctry;
  }

  public ServiceSchema universe() {
    return thrift().schema().findService("everything", "Universe");
  }

  @Test
  public void testCallMessage() throws TException {
    debug(
      "----------------------------- " + 
      "Entering testCallMessage() for " + fctry.getClass().getName()
    );
    final byte[] msgBytes = createCallMessage(fctry);
    final TMemoryInputTransport inTrans = new TMemoryInputTransport(msgBytes);
    final TProtocol inProto = fctry.getProtocol(inTrans);
    final ThriftParserHandler listener = new ParserListener();
    final ThriftParser parser = new ThriftParser(
      thrift().schema(), listener
    );
    parser.readMessage(universe(), inProto);
    debug(
      "----------------------------- " + 
      "Exiting testCallMessage() for " + fctry.getClass().getName()
    );
  }

  @Test
  public void testReplyMessage() throws TException {
    debug(
      "----------------------------- Entering testReplyMessage() for " + 
      fctry.getClass().getName()
    );
    final byte[] msgBytes = createCallMessage(fctry);
    final TMemoryInputTransport inTrans = new TMemoryInputTransport(msgBytes);
    final TProtocol inProto = fctry.getProtocol(inTrans);
    final ThriftParserHandler listener = new ParserListener();
    final ThriftParser parser = new ThriftParser(
      thrift().schema(), listener
    );
    parser.readMessage(thrift().schema().findService("everything", "Universe"), inProto);
    debug(
      "----------------------------- Exiting testReplyMessage() for " + 
      fctry.getClass().getName()
    );
  }

  @Test
  public void testReadStruct() throws TException {
    debug(
      "----------------------------- Entering testReadStruct() for " + 
      fctry.getClass().getName()
    );

    final TByteArrayOutputStream baos = new TByteArrayOutputStream();
    final Everything original = Examples.everythingStruct();
    final TProtocol proto = fctry.getProtocol(new TIOStreamTransport(baos));

    final TByteArrayOutputStream baos2 = new TByteArrayOutputStream();
    final TTransport outTrans = new TIOStreamTransport(baos2);
    final TProtocol outProto = new TBinaryProtocol(outTrans);
    final ThriftParserHandler handler = new ThriftParserHandlerChain(
      new ParserListener(),
      new TProtocolProxy(outProto)
    );
    final ThriftParser parser = new ThriftParser(thrift().schema(), handler);

    original.write(proto);
    final byte[] msgBytes = baos.toByteArray();
    final TMemoryInputTransport inTrans = new TMemoryInputTransport(msgBytes);
    final TProtocol inProto = fctry.getProtocol(inTrans);
    parser.readStruct((StructSchema)thrift().schema().findType("everything", "Everything"), inProto);

    final InputStream bais = new ByteArrayInputStream(baos2.toByteArray());
    final TTransport testTrans = new TIOStreamTransport(bais);
    final TProtocol testProto = new TBinaryProtocol(testTrans);
    Everything roundtripped = new Everything();
    roundtripped.read(testProto);

    debug(" original: %s", original);
    debug("roundtrip: %s", roundtripped);

    assertEquals(original, roundtripped);

    debug(
      "----------------------------- Exiting testReadStruct() for " + 
      fctry.getClass().getName()
    );
  }

  byte[] createCallMessage(TProtocolFactory fctry) throws TException {
    final TByteArrayOutputStream baos = new TByteArrayOutputStream();
    final Everything o = Examples.everythingStruct();
    final TProtocol proto = fctry.getProtocol(new TIOStreamTransport(baos));
    final Universe.Client cl = new Universe.Client.Factory().getClient(proto);
    cl.send_grok(o);
    return baos.toByteArray();
  }

  byte[] createReplyMessage(TProtocolFactory fctry) throws TException {
    final TByteArrayOutputStream baos = new TByteArrayOutputStream();
    final Everything o = Examples.everythingStruct();
    final TProtocol proto = fctry.getProtocol(new TIOStreamTransport(baos));
    final Universe.Client cl = new Universe.Client.Factory().getClient(proto);
    cl.send_grok(o);
    return baos.toByteArray();
  }

  private static final void debug(String fmt, Object... args) {
    System.err.print(String.format(fmt + "%n", args));
  }

  static final class ParserListener implements ThriftParserHandler {

    private final StringBuilder indent = new StringBuilder();

    @Override
    public void onMessageBegin(TMessage msg, MethodSchema schema) throws TException {
      logOpen("message", schema);
    }

    @Override
    public void onMessageEnd() throws TException {
      logClose("message");
    }

    @Override
    public void onStructBegin(
        final TStruct struct, 
        final AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
      logOpen("struct", schema);
    }

    @Override
    public void onStructEnd() throws TException {
      logClose("struct");
    }

    @Override
    public void onFieldBegin(TField field, AbstractFieldSchema<?, ?> schema) 
        throws TException {
      logOpen("field", schema);
    }

    @Override
    public void onFieldEnd() throws TException {
      logClose("field");
    }

    @Override
    public void onFieldStop() throws TException {
    }

    @Override
    public void onMapBegin(TMap map) throws TException {
      logOpen("map", toString(map));
    }

    @Override
    public void onMapEnd() throws TException {
      logClose("map");
    }

    @Override
    public void onListBegin(TList list) throws TException {
      logOpen("list", toString(list));
    }

    @Override
    public void onListEnd() throws TException {
      logClose("list");
    }

    @Override
    public void onSetBegin(TSet set) throws TException {
      logOpen("set", toString(set));
    }

    @Override
    public void onSetEnd() throws TException {
      logClose("set");
    }

    @Override
    public void onBool(boolean val) throws TException {
      logOpen("bool", val);
      indentDown();
    }

    @Override
    public void onByte(byte val) throws TException {
      logOpen("i8", val);
      indentDown();
    }

    @Override
    public void onI16(short val) throws TException {
      logOpen("i16", val);
      indentDown();
    }

    @Override
    public void onI32(int val) throws TException {
      logOpen("i32", val);
      indentDown();
    }

    @Override
    public void onI64(long val) throws TException {
      logOpen("i64", val);
      indentDown();
    }

    @Override
    public void onString(String val) throws TException {
      logOpen("string", val);
      indentDown();
    }

    @Override
    public void onBinary(ByteBuffer val) throws TException {
      ByteBuffer cpy = val.duplicate();
      final byte[] bytes = new byte[val.remaining()];
      cpy.get(bytes);
      logOpen("binary", new BigInteger(bytes).toString(16));
      indentDown();
    }

    @Override
    public void onDouble(double val) throws TException {
      logOpen("double", val);
      indentDown();
    }

    @Override
    public void onEnum(int val, EnumSchema schema) throws TException {
      logOpen("enum", val + "(" + schema.getName() + ")");
      indentDown();
    }

    private void indentUp() {
      indent.append("  ");
    }

    private void indentDown() {
      if (indent.length() < 2) {
        throw new IllegalStateException("cannot indent down");
      }
      indent.setLength(indent.length() - 2);
    }

    private void logOpen(String type, Object val) {
      debug("%s(%s:%s)", indent, type, val);
      indentUp();
    }

    private void logClose(String type) {
      indentDown();
      debug("%s(/%s)", indent, type);
    }

    static final String toString(TStruct str) {
      return "<TStruct name:" + str.name + ">";
    }

    static final String toString(TMap map) {
      return String.format(
        "<TMap keyType:%s valueType:%s size:%s>",
        map.keyType, map.valueType, map.size
      );
    }

    static final String toString(TSet set) {
      return String.format("<TSet elemType:%s size:%s>", set.elemType, set.size);
    }

    static final String toString(TList l) {
      return String.format("<TList elemType:%s size:%s>", l.elemType, l.size);
    }

  }

}
