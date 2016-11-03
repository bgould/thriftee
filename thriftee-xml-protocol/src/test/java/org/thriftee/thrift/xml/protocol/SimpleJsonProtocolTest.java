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

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.thrift.schema.IdlSchemaBuilder;
import org.thriftee.thrift.schema.ModuleSchema;
import org.thriftee.thrift.schema.SchemaBuilderException;
import org.thriftee.thrift.schema.ServiceSchema;
import org.thriftee.thrift.schema.StructSchema;
import org.thriftee.thrift.schema.ThriftSchema;
import org.thriftee.thrift.xml.BaseThriftProtocolTest;
import org.thriftee.thrift.xml.TestCall;
import org.thriftee.thrift.xml.TestObject;

@RunWith(Parameterized.class)
public class SimpleJsonProtocolTest extends BaseThriftProtocolTest {

  @Parameters
  public static Collection<Object[]> data() {
    return testParameters();
  }

  private final TestObject testobj;

  private IdlSchemaBuilder bldr = new IdlSchemaBuilder();

  public SimpleJsonProtocolTest(TestObject testobj) {
    super();
    this.testobj = testobj;
  }

  @Test
  public void testRoundTrip()
      throws TException, UnsupportedEncodingException,
              IllegalAccessException, InstantiationException {

    final TJsonApiProtocol.Factory fctry = new TJsonApiProtocol.Factory();
    final ThriftSchema schema = schema(testobj.module);
    final ModuleSchema module = schema.findModule(testobj.module);
    final TMemoryBuffer writeBuffer = new TMemoryBuffer(4096);
    LOG.debug("Writing to JSON: " + testobj);
    if (testobj instanceof TestCall) {
      final TestCall call = (TestCall) testobj;
      final ServiceSchema svc = schema.findService(call.module, call.service);
      final TJsonApiProtocol proto = fctry.getProtocol(writeBuffer);
      proto.setBaseService(svc);
      proto.writeMessageBegin(call.getTMessage());
      call.obj.write(proto);
      proto.writeMessageEnd();
    } else {
      final StructSchema struct = module.getStructs().get(testobj.struct);
      final TJsonApiProtocol proto = fctry.getProtocol(writeBuffer);
      proto.setBaseStruct(struct);
      testobj.obj.write(proto);
    }
    LOG.debug("\n------------------------\n" +
              writeBuffer.toString("UTF-8") +
              "\n------------------------");
    LOG.debug("Reading from JSON");
    final TMemoryInputTransport readBuffer = new TMemoryInputTransport(
      writeBuffer.getArray(), 0, writeBuffer.length()
    );
    final TBase<?, ?> newobj = testobj.obj.getClass().newInstance();
    if (testobj instanceof TestCall) {
      final TestCall call = (TestCall) testobj;
      final ServiceSchema svc = schema.findService(call.module, call.service);
      final TJsonApiProtocol proto = fctry.getProtocol(readBuffer);
      proto.setBaseService(svc);
      final TMessage msg = proto.readMessageBegin();
      assertEquals(call.type, msg.type);
      assertEquals(call.method, msg.name);
      newobj.read(proto);
      proto.readMessageEnd();
    } else {
      final StructSchema struct = module.getStructs().get(testobj.struct);
      final TJsonApiProtocol proto = fctry.getProtocol(writeBuffer);
      proto.setBaseStruct(struct);
      newobj.read(proto);
    }
    LOG.debug("Completed read: " + newobj);

    assertEquals(testobj.obj, newobj);
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

}
