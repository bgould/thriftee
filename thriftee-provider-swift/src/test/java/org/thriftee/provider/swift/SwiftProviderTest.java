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
package org.thriftee.provider.swift;

import static org.thriftee.provider.swift.SwiftSchemaProvider.moduleNameFor;
import static org.thriftee.provider.swift.SwiftSchemaProvider.serviceNameFor;

import java.io.ByteArrayOutputStream;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.Test;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.core.ThriftStartupException;
import org.thriftee.provider.swift.tests.AbstractSwiftTest;
import org.thriftee.provider.swift.tests.CalculatorService;
import org.thriftee.provider.swift.tests.Op;
import org.thriftee.provider.swift.tests.Work;

import com.facebook.swift.codec.ThriftCodec;

public class SwiftProviderTest extends AbstractSwiftTest {

  public static final String MODULE = TEST_MODULE;

  public SwiftProviderTest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testModuleName() throws Exception {
    final String packageName = CalculatorService.class.getPackage().getName();
    final String moduleName = moduleNameFor(packageName);
    Assert.assertEquals(MODULE, moduleName);
  }

  @Test
  public void testServiceName() throws Exception {
    final String serviceName = serviceNameFor(CalculatorService.class);
    final String expected = MODULE + ".CalculatorService";
    Assert.assertEquals(expected, serviceName);
  }

  @Test
  public void testProcessorLookup() throws Exception {
    ModuleSchema module = thrift().schema().getModules().get(MODULE);
    ServiceSchema svc = module.getServices().get("CalculatorService");
    TProcessor processor = thrift().processorFor(svc);
    Assert.assertNotNull("TProcessor should not be null", processor);
  }

  @Test
  public void testMultiplexProcessor() throws Exception {
    TMultiplexedProcessor mp = thrift().multiplexedProcessor();
    LOG.debug("multiplexed processor: {}", mp);
  }

  @Test
  public void testWriteStruct() throws Exception {

    ThriftCodec<Work> codec = thriftCodecManager().getCodec(Work.class);
    Assert.assertNotNull(codec);

    Work work = new Work();
    work.operation = Op.ADD;
    work.operand1 = 4;
    work.operand2 = 2;

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TTransport transport = new TIOStreamTransport(baos);
    TProtocol protocol = new TSimpleJSONProtocol(transport);
    codec.write(work, protocol);

    byte[] bytes = baos.toByteArray();
    Assert.assertTrue("byte array with result has length > 0", bytes.length > 0);

    LOG.debug("Serialized object: {}", new String(bytes));

  }

}
