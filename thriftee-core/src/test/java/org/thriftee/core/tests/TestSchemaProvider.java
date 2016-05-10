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
package org.thriftee.core.tests;

import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_001;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_016;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.thrift.TProcessor;
import org.thriftee.core.SchemaProvider;
import org.thriftee.core.ServiceLocator;
import org.thriftee.core.ServiceLocatorException;
import org.thriftee.core.ThriftStartupException;
import org.thriftee.core.util.FileUtil;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.UserService;

import everything.Universe;

public class TestSchemaProvider implements SchemaProvider {

  @Override
  public SortedMap<String, TProcessor> 
      buildProcessorMap(ServiceLocator arg0) throws ThriftStartupException {
    try {
      final SortedMap<String, TProcessor> map = new TreeMap<>();
      final Universe.Iface universe = arg0.locate(Universe.Iface.class);
      final UserService.Iface userIf = arg0.locate(UserService.Iface.class);
      final GroupService.Iface groupIf = arg0.locate(GroupService.Iface.class);
      addProcessor(Universe.class, new Universe.Processor<>(universe), map);
      addProcessor(UserService.class, new UserService.Processor<>(userIf), map);
      addProcessor(GroupService.class, new GroupService.Processor<>(groupIf), map);
      return map;
    } catch (ServiceLocatorException e) {
      throw new ThriftStartupException(e, STARTUP_016);
    }
  }

  private void addProcessor(
      final Class<?> svc, 
      final TProcessor proc, 
      final SortedMap<String, TProcessor> map) {
    final String module = svc.getPackage().getName().replace('.', '_');
    final String service = svc.getSimpleName();
    final String name = module + "." + service;
    map.put(name, proc);
  }

  @Override
  public File[] exportIdl(File idlDir) throws ThriftStartupException {
    final String[] rsrcs = new String[] {
      "everything.thrift",
      "nothing_all_at_once.thrift",
      "org_thriftee_examples_usergroup_domain.thrift",
      "org_thriftee_examples_usergroup_service.thrift"
    };
    final StringBuilder global = new StringBuilder();
    try {
      if (idlDir.exists()) {
        FileUtil.deleteRecursively(idlDir);
      }
      if (!idlDir.mkdirs()) {
        throw new IOException("could not create idlDir: " + idlDir);
      }
      for (String rsrc : rsrcs) {
        final File idlFile = new File(idlDir, rsrc);
        if (idlFile.exists()) {
          throw new IOException("IDL file already exists: " + idlFile);
        }
        FileUtil.copyResourceToDir(rsrc, idlDir);
        if (!idlFile.exists()) {
          throw new IOException("IDL should have been copied: " + idlFile);
        }
        global.append(String.format("include \"%s\"%n", rsrc));
      }
      final File globalFile = new File(idlDir, "global.thrift");
      FileUtil.writeStringToFile(global.toString(), globalFile, FileUtil.UTF_8);
      return idlDir.listFiles();
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_001, e.getMessage());
    }
    
  }

}
