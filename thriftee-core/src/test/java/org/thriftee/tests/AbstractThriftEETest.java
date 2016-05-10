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
package org.thriftee.tests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.exceptions.ThriftSystemException;
import org.thriftee.framework.SchemaProvider;
import org.thriftee.framework.ThriftEE;
import org.thriftee.framework.ThriftEEConfig;

public abstract class AbstractThriftEETest {

  private final File tempDirForClass;

  private static final Map<String, ThriftEE> thrifteeInstances = new HashMap<String, ThriftEE>();

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public static final String USERGROUP_SERVICES_MODULE = 
                    UserService.class.getPackage().getName().replace('.', '_');

  protected static ThriftEE loadThriftee(
        File tempDir, SchemaProvider schemaProvider, boolean generateClients
      ) throws ThriftSystemException {
    synchronized (thrifteeInstances) {
      if (!thrifteeInstances.containsKey(tempDir.getAbsolutePath())) {
        final ThriftEE thrift = new ThriftEE(
          (new ThriftEEConfig.Builder())
            .schemaProvider(schemaProvider)
            .serviceLocator(new TestServiceLocator())
            .useDefaultClientTypeAliases(generateClients)
            .tempDir(tempDir)
            .build()
        );
        thrifteeInstances.put(tempDir.getAbsolutePath(), thrift);
      }
    }
    return thrifteeInstances.get(tempDir.getAbsolutePath());
  }

  private final ThriftEE thrift;

  public AbstractThriftEETest() {
    try {
      final String simpleName = getClass().getSimpleName();
      final String prefix = System.getProperty("thriftee.build.dir", "target");
      final File tempDir = new File(prefix + "/tests/" + simpleName);
      this.tempDirForClass = tempDir;
      final SchemaProvider schemaProvider = new TestSchemaProvider();
      this.thrift = loadThriftee(tempDir, schemaProvider, generateClients());
    } catch (ThriftSystemException e) {
      throw new RuntimeException(e);
    }
  }

  protected File getTempDirForTest() {
    final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
    File retval = new File(tempDirForClass, stackTraceElement.getMethodName());
    return retval;
  }

  protected ThriftEE thrift() {
    return thrift;
  }

  protected boolean generateClients() {
    return false;
  }

}
