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

import static org.thriftee.provider.swift.SwiftSchemaProvider.moduleNameFor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.schema.XMLSchemaBuilder;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.exceptions.ThriftSystemException;
import org.thriftee.framework.SchemaProvider;
import org.thriftee.framework.ThriftEE;
import org.thriftee.framework.ThriftEEConfig;
import org.thriftee.provider.swift.SwiftSchemaProvider;
import org.thriftee.util.FileUtil;

import com.facebook.swift.codec.ThriftCodecManager;

public abstract class AbstractThriftEETest {

  private final SwiftSchemaProvider schemaProvider;

  private final File tempDirForClass;

  private static final Map<String, ThriftEE> thrifteeInstances = new HashMap<String, ThriftEE>();

  private static final Properties TEST_PROPERTIES;

  private static final File thriftLibDir;

  private static final File thriftExecutable;

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public static final String USERGROUP_SERVICES_MODULE = 
                    moduleNameFor(UserService.class.getPackage().getName());

  static {
    final Logger logger = LoggerFactory.getLogger(AbstractThriftEETest.class);
    logger.trace("TRACE level enabled");
    logger.debug("DEBUG level enabled");
    logger.info( " INFO level enabled");
    logger.warn( " WARN level enabled");
    logger.error("ERROR level enabled");
    final ClassLoader ldr = AbstractThriftEETest.class.getClassLoader();
    final URL propertiesResource = ldr.getResource("thriftee.test.properties");
    if (propertiesResource != null) {
      try (final InputStream inputStream = propertiesResource.openStream()){
        TEST_PROPERTIES = FileUtil.readProperties(inputStream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      TEST_PROPERTIES = new Properties();
    }
    thriftLibDir = new File(TEST_PROPERTIES.getProperty(
      "thrift.lib", System.getProperty("thrift.lib", "/usr/local/src/thrift/lib")
    ));
    thriftExecutable = new File(TEST_PROPERTIES.getProperty(
      "thrift.exe", System.getProperty("thrift.exe", "/usr/local/bin/thrift")
    ));
  }

  public static final File thriftLibDir() {
    return thriftLibDir;
  }

  public static final File thriftExecutable() {
    return thriftExecutable;
  }

  public ThriftCodecManager thriftCodecManager() {
    return schemaProvider.codecManager();
  }

  protected static ThriftEE loadThriftee(
      File tempDir, SchemaProvider schemaProvider) throws ThriftSystemException {
    synchronized (thrifteeInstances) {
      if (!thrifteeInstances.containsKey(tempDir.getAbsolutePath())) {
        final ThriftEE thrift = new ThriftEE(
          (new ThriftEEConfig.Builder())
            .schemaBuilder(new XMLSchemaBuilder())
            .schemaProvider(schemaProvider)
            .serviceLocator(new ExampleServiceLocator())
            .thriftLibDir(thriftLibDir)
            .thriftExecutable(thriftExecutable)
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
      this.schemaProvider = new SwiftSchemaProvider(true, new TestClasspath());
      this.thrift = loadThriftee(tempDir, schemaProvider);
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

}
