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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializable;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.thrift.protocol.xml.BaseThriftXMLTest;
import org.thriftee.thrift.protocol.xml.TestCall;
import org.thriftee.thrift.protocol.xml.TestObject;
import org.thriftee.thrift.protocol.xml.Transforms;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class TSoapXmlProtocolTest extends BaseThriftXMLTest {

  private static final Transforms transforms = new Transforms();

  public static TSoapXmlProtocol.Factory factory;

  @BeforeClass
  public static void createFactory() {
    factory = new TSoapXmlProtocol.Factory();
    factory.setTransforms(transforms);
    factory.setModelFile(modelFor("everything"));
    try {
      transforms.preload(factory.getModelFile());
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Parameters
  public static Collection<Object[]> data() {
    return testParameters();
  }

  private final TestObject testobj;

  public TSoapXmlProtocolTest(TestObject testobj) {
    super();
    this.testobj = testobj;
  }

  @Test
  public void testRoundTrip()
      throws TException, UnsupportedEncodingException, IOException,
              IllegalAccessException, InstantiationException, SAXException {

    final TMemoryBuffer writeBuffer = new TMemoryBuffer(4096);
    LOG.debug("Writing to XML: " + testobj);
    if (testobj instanceof TestCall) {
      final TestCall call = (TestCall) testobj;
      final TSoapXmlProtocol proto = factory.getProtocol(writeBuffer);
      proto.setModuleName(call.module);
      proto.setServiceName(call.service);
      proto.writeMessageBegin(call.getTMessage());
      call.obj.write(proto);
      proto.writeMessageEnd();
    } else {
      final TSoapXmlProtocol proto = factory.getProtocol(writeBuffer);
      proto.setModuleName(testobj.module);
      proto.setStructName(testobj.struct);
      testobj.obj.write(proto);

      final File xsd = schemaFor(testobj.module);
      TXMLProtocol.XML.validate(xsd.toURI().toURL(), new StreamSource(
          new ByteArrayInputStream(
              writeBuffer.getArray(), 0, writeBuffer.length())));
      System.out.println("validated " + testobj.name + " against schema.");
    }
    LOG.debug("\n------------------------\n" +
              writeBuffer.toString("UTF-8") +
              "\n------------------------");

    LOG.debug("Reading from XML");
    final TMemoryInputTransport readBuffer = new TMemoryInputTransport(
      writeBuffer.getArray(), 0, writeBuffer.length()
    );
    final TSerializable newobj = testobj.obj.getClass().newInstance();
    if (testobj instanceof TestCall) {
      final TestCall call = (TestCall) testobj;
      final TSoapXmlProtocol proto = factory.getProtocol(readBuffer);
      proto.setModuleName(call.module);
      proto.setServiceName(call.service);
      final TMessage msg = proto.readMessageBegin();
      assertEquals(call.type, msg.type);
      assertEquals(call.method, msg.name);
      newobj.read(proto);
      proto.readMessageEnd();
    } else {
      final TSoapXmlProtocol proto = factory.getProtocol(readBuffer);
      proto.setModuleName(testobj.module);
      proto.setStructName(testobj.struct);
      newobj.read(proto);
    }
    LOG.debug("Completed read: " + newobj);

//    final TMemoryBuffer writeBuffer2 = new TMemoryBuffer(4096);
//    if (testobj instanceof TestCall) {
//      final TestCall call = (TestCall) testobj;
//      final ServiceSchema svc = schema.findService(call.module, call.service);
//      final TSOAPProtocol proto = fctry.getProtocol(writeBuffer2);
//      proto.setBaseService(svc);
//      proto.writeMessageBegin(call.getTMessage());
//      newobj.write(proto);
//      proto.writeMessageEnd();
//    } else {
//      final StructSchema struct = module.getStructs().get(testobj.struct);
//      final TSOAPProtocol proto = fctry.getProtocol(writeBuffer2);
//      proto.setBaseStruct(struct);
//      newobj.write(proto);
//    }
//    LOG.debug("\n------------------------\n" +
//              writeBuffer2.toString("UTF-8") +
//              "\n------------------------");

    if (newobj instanceof TApplicationException) {
      final TApplicationException exObj = (TApplicationException) testobj.obj;
      final TApplicationException exNew = (TApplicationException) newobj;
      assertEquals(exObj.getType(), exNew.getType());
      assertEquals(exObj.getMessage(), exNew.getMessage());
    } else {
      assertEquals(testobj.obj, newobj);
    }
  }

}
