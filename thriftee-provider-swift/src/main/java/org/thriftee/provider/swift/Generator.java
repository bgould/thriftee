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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.facebook.swift.generator.swift2thrift.Swift2ThriftGenerator;
import com.facebook.swift.generator.swift2thrift.Swift2ThriftGeneratorConfig;

public class Generator {

  private Class<?>[] classes;

  private final Map<String, Set<Class<?>>> packageMap = new TreeMap<>();

  private final Map<String, String> includeMap = new TreeMap<String, String>();

  private File tempDir;

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  public Generator() {
  }

  public void setClasses(Class<?>[] _classes) {
    this.classes = _classes;
  }

  public void setTempDir(File _tempDir) {
    if (!_tempDir.isDirectory() || !_tempDir.canWrite()) {
      throw new IllegalArgumentException("temp directory is not writeable");
    }
    tempDir = _tempDir;
  }
  
  // TODO: this is an ugly hack that needs an upstream fix to be removed
  private static Swift2ThriftGenerator forConfig(Swift2ThriftGeneratorConfig config) {
    try {
      final Constructor<Swift2ThriftGenerator> ctor = 
        Swift2ThriftGenerator.class.getDeclaredConstructor(new Class[] {
          Swift2ThriftGeneratorConfig.class
        });
      ctor.setAccessible(true);
      return ctor.newInstance(config);
    } catch (NoSuchMethodException|IllegalAccessException|InstantiationException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  public File generate() throws IOException {
    init();
    for (Map.Entry<String, Set<Class<?>>> pkg : packageMap.entrySet()) {
      File outputFile = new File(tempDir, makeThriftFilename(pkg.getKey()));
      Swift2ThriftGeneratorConfig config = createConfig(outputFile, pkg.getKey());
      Swift2ThriftGenerator generator = forConfig(config);
      List<String> classNames = new ArrayList<String>(pkg.getValue().size());
      for (Class<?> klass : pkg.getValue()) {
        classNames.add(klass.getName());
      }
      generator.parse(classNames);
    }
    return tempDir;
  }

  private void init() throws IOException {
    packageMap.clear();
    includeMap.clear();
    if (tempDir == null) {
      tempDir = File.createTempFile("swift_generator_", "");
      tempDir.delete();
      tempDir.mkdir();
    }
    if (!tempDir.isDirectory() || !tempDir.canWrite()) {
      throw new IllegalStateException("temp directory is not writeable");
    }
    for (Class<?> klass : classes) {
      String packageName = klass.getPackage().getName();
      Set<Class<?>> set = packageMap.get(packageName);
      if (set == null) {
        set = new HashSet<Class<?>>();
        packageMap.put(packageName, set);
      }
      set.add(klass);
      includeMap.put(klass.getName(), makeThriftFilename(packageName));
    }
    LOG.trace("[Generator] final include map: " + includeMap);
    LOG.trace("[Generator] final package map: " + packageMap);
  }

  private Swift2ThriftGeneratorConfig createConfig(
      final File _outputFile, 
      final String _packageName) {
    return Swift2ThriftGeneratorConfig
          .builder()
          .outputFile(_outputFile)
          .includeMap(includeMap)
          .defaultPackage(_packageName)
          //.verbose(true)
          .build();
  }

  private String makeThriftFilename(String _packageName) {
    return SwiftSchemaProvider.moduleNameFor(_packageName) + ".thrift";
  }

}
