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
package org.thriftee.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;

import org.apache.thrift.TException;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.Test;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.idl.IdlSchema;
import org.thriftee.core.tests.AbstractThriftEETest;
import org.thriftee.examples.usergroup.domain.User;
import org.thriftee.examples.usergroup.service.UserService;

public class ThriftEETest extends AbstractThriftEETest {

  public static final String MODULE = USERGROUP_SERVICES_MODULE;

  public static final String MODULE_META = ThriftEE.MODULE_NAME_META_IDL;

  public ThriftEETest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testServiceLocator() throws Exception {
    UserService.Iface userService;
    userService = thrift().serviceLocator().locate(UserService.Iface.class);
    User aardvark = userService.find("aaardvark");
    assertNotNull("returned user must not be null", aardvark);
  }

  @Test
  public void testProcessorLookup() throws Exception {

    ModuleSchema moduleSchema = thrift().schema().getModules().get(MODULE);
    ServiceSchema svcSchema = moduleSchema.getServices().get("GroupService");
    TProcessor groupService = thrift().processorFor(svcSchema);
    assertNotNull("TProcessor should not be null", groupService);

  }

  @Test
  public void testThriftServiceProcessor() throws Exception {

    final ModuleSchema meta = thrift().schema().getModules().get(MODULE_META);
    final ServiceSchema svc = meta.getServices().get("ThriftSchemaService");
    final TProcessor processor = thrift().processorFor(svc);
    Assert.assertNotNull("TProcessor should not be null", processor);

    final byte[] callBytes = createBinaryThriftServiceSchemaCall();
    final TTransport intrans = new TMemoryInputTransport(callBytes);
    final TBinaryProtocol in = new TBinaryProtocol(intrans);

    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final TTransport outtrans = new TIOStreamTransport(outStream);
    final TBinaryProtocol out = new TBinaryProtocol(outtrans);
    processor.process(in, out);
    outtrans.flush();

    final TTransport read = new TMemoryInputTransport(outStream.toByteArray());
    final TBinaryProtocol readProto = new TBinaryProtocol(read);
    final TMessage msg = readProto.readMessageBegin();
    assertEquals(TMessageType.REPLY, msg.type);
    assertEquals("getSchema", msg.name);
    assertEquals(1, msg.seqid);

    final TStruct result = readProto.readStructBegin();
    assertNotNull(result);

    final TField resultField = readProto.readFieldBegin();
    assertNotNull(resultField);
    assertEquals(0, resultField.id);
    assertEquals(TType.STRUCT, resultField.type);

    final IdlSchema schema = new IdlSchema();
    schema.read(readProto);
    readProto.readFieldEnd();

    System.out.println(schema);

    final TField stop = readProto.readFieldBegin();
    assertEquals(TType.STOP, stop.type);

    readProto.readStructEnd();
    readProto.readMessageEnd();

  }

  public static byte[] createBinaryThriftServiceSchemaCall() throws TException {
    final ByteArrayOutputStream inStream = new ByteArrayOutputStream();
    final TTransport intrans = new TIOStreamTransport(inStream);
    final TBinaryProtocol in = new TBinaryProtocol(intrans);
    in.writeMessageBegin(new TMessage("getSchema", TMessageType.CALL, 1));
    in.writeStructBegin(new TStruct("getSchema_args"));
    in.writeFieldStop();
    in.writeStructEnd();
    in.writeMessageEnd();
    intrans.flush();
    return inStream.toByteArray();
  }

  @Test
  public void testMultiplexProcessor() throws Exception {
    TMultiplexedProcessor mp = thrift().multiplexedProcessor();
    LOG.debug("multiplexed processor: {}", mp);
  }

}
