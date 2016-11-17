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
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
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
import org.thriftee.thrift.protocol.xml.BaseThriftProtocolTest;
import org.thriftee.thrift.protocol.xml.TestCall;
import org.thriftee.thrift.protocol.xml.TestObject;
import org.thriftee.thrift.schema.IdlSchemaBuilder;
import org.thriftee.thrift.schema.ModuleSchema;
import org.thriftee.thrift.schema.SchemaBuilderException;
import org.thriftee.thrift.schema.ServiceSchema;
import org.thriftee.thrift.schema.StructSchema;
import org.thriftee.thrift.schema.ThriftSchema;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class TSOAPProtocolTest extends BaseThriftProtocolTest {

  @Parameters
  public static Collection<Object[]> data() {
    return testParameters();
  }

  private final TestObject testobj;

  private IdlSchemaBuilder bldr = new IdlSchemaBuilder();

  public TSOAPProtocolTest(TestObject testobj) {
    super();
    this.testobj = testobj;
  }

  @Test
  public void testRoundTrip()
      throws TException, UnsupportedEncodingException, IOException,
              IllegalAccessException, InstantiationException, SAXException {

    final TSOAPProtocol.Factory fctry = new TSOAPProtocol.Factory();
    final ThriftSchema schema = schema(testobj.module);
    final ModuleSchema module = schema.findModule(testobj.module);
    final TMemoryBuffer writeBuffer = new TMemoryBuffer(4096);
    LOG.debug("Writing to XML: " + testobj);
    if (testobj instanceof TestCall) {
      final TestCall call = (TestCall) testobj;
      final ServiceSchema svc = schema.findService(call.module, call.service);
      final TSOAPProtocol proto = fctry.getProtocol(writeBuffer);
      proto.setBaseService(svc);
      proto.writeMessageBegin(call.getTMessage());
      call.obj.write(proto);
      proto.writeMessageEnd();
    } else {
      final StructSchema struct = module.getStructs().get(testobj.struct);
      final TSOAPProtocol proto = fctry.getProtocol(writeBuffer);
      proto.setBaseStruct(struct);
      testobj.obj.write(proto);

      final File xsd = new File(testDir, module.getName() + ".xsd");
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
      final ServiceSchema svc = schema.findService(call.module, call.service);
      final TSOAPProtocol proto = fctry.getProtocol(readBuffer);
      proto.setBaseService(svc);
      final TMessage msg = proto.readMessageBegin();
      assertEquals(call.type, msg.type);
      assertEquals(call.method, msg.name);
      newobj.read(proto);
      proto.readMessageEnd();
    } else {
      final StructSchema struct = module.getStructs().get(testobj.struct);
      final TSOAPProtocol proto = fctry.getProtocol(readBuffer);
      proto.setBaseStruct(struct);
      newobj.read(proto);
    }
    LOG.debug("Completed read: " + newobj);

    final TMemoryBuffer writeBuffer2 = new TMemoryBuffer(4096);
    if (testobj instanceof TestCall) {
      final TestCall call = (TestCall) testobj;
      final ServiceSchema svc = schema.findService(call.module, call.service);
      final TSOAPProtocol proto = fctry.getProtocol(writeBuffer2);
      proto.setBaseService(svc);
      proto.writeMessageBegin(call.getTMessage());
      newobj.write(proto);
      proto.writeMessageEnd();
    } else {
      final StructSchema struct = module.getStructs().get(testobj.struct);
      final TSOAPProtocol proto = fctry.getProtocol(writeBuffer2);
      proto.setBaseStruct(struct);
      newobj.write(proto);
    }
    LOG.debug("\n------------------------\n" +
              writeBuffer2.toString("UTF-8") +
              "\n------------------------");

    if (newobj instanceof TApplicationException) {
      final TApplicationException exObj = (TApplicationException) testobj.obj;
      final TApplicationException exNew = (TApplicationException) newobj;
      assertEquals(exObj.getType(), exNew.getType());
      assertEquals(exObj.getMessage(), exNew.getMessage());
    } else {
      assertEquals(testobj.obj, newobj);
    }
  }

  public ThriftSchema schema(String model) {
    try {
      final StreamSource src = new StreamSource(modelFor(model));
      final ThriftSchema thriftSchema = bldr.buildFromXml(src);
      return thriftSchema;
    } catch (SchemaBuilderException sbe) {
      throw new RuntimeException(sbe);
    }
  }

  @BeforeClass
  public static void makeXsd() throws Exception {

    final TransformerFactory fctry = TransformerFactory.newInstance();

    final URL xsdTransform = TSOAPProtocol.class.getClassLoader().getResource(
      "org/thriftee/thrift/xml/thrift-model-to-xsd2.xsl"
    );
    final Transformer xsdTransformer = fctry.newTransformer(
      new StreamSource(xsdTransform.openStream())
    );

    final File wsdlTransform = new File("src/main/resources",
        "org/thriftee/thrift/xml/thrift-model-to-wsdl2.xsl");
//    final URL wsdlTransform = TSOAPProtocol.class.getClassLoader().getResource(
//      "org/thriftee/thrift/xml/thrift-model-to-wsdl2.xsl"
//    );
    final Transformer wsdlTransformer = fctry.newTransformer(
      new StreamSource(wsdlTransform)
    );

    {
      final StreamSource src = new StreamSource(modelFor("everything"));
      final StreamResult res = new StreamResult(new File(testDir, "everything.xsd"));
      xsdTransformer.setParameter("root_module", "everything");
      xsdTransformer.transform(src, res);
    }

    {
      final StreamSource src2 = new StreamSource(modelFor("nothing_all_at_once"));
      final StreamResult res2 = new StreamResult(new File(testDir, "nothing_all_at_once.xsd"));
      xsdTransformer.setParameter("root_module", "nothing_all_at_once");
      xsdTransformer.transform(src2, res2);
    }

    {
      final StreamSource src = new StreamSource(modelFor("everything"));
      final StreamResult res = new StreamResult(new File(testDir, "everything.Universe.wsdl"));
      wsdlTransformer.setParameter("service_module", "everything");
      wsdlTransformer.setParameter("service_name", "Universe");
      wsdlTransformer.transform(src, res);
    }

    {
      final StreamSource src = new StreamSource(modelFor("everything"));
      final StreamResult res = new StreamResult(new File(testDir, "nothing_all_at_once.Metaverse.wsdl"));
      wsdlTransformer.setParameter("service_module", "nothing_all_at_once");
      wsdlTransformer.setParameter("service_name", "Metaverse");
      wsdlTransformer.transform(src, res);
    }
  }

}
