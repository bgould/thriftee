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
package org.thriftee.framework;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.examples.usergroup.domain.User;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.tests.AbstractThriftEETest;

public class ThriftEETest extends AbstractThriftEETest {

  public static final String MODULE = USERGROUP_SERVICES_MODULE;

  public ThriftEETest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testServiceLocator() throws Exception {
    UserService.Iface userService;
    userService = thrift().serviceLocator().locate(UserService.Iface.class);
    User aardvark = userService.find("aaardvark");
    Assert.assertNotNull("returned user must not be null", aardvark);
  }

  @Test
  public void testProcessorLookup() throws Exception {
    ModuleSchema moduleSchema = thrift().schema().getModules().get(MODULE);
    ServiceSchema svcSchema = moduleSchema.getServices().get("GroupService");
    TProcessor groupService = thrift().processorFor(svcSchema);
    Assert.assertNotNull("TProcessor should not be null", groupService);
  }

  @Test
  public void testMultiplexProcessor() throws Exception {
    TMultiplexedProcessor mp = thrift().multiplexedProcessor();
    LOG.debug("multiplexed processor: {}", mp);
  }

}
