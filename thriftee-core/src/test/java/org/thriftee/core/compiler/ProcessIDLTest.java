package org.thriftee.core.compiler;
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
/*
package org.thriftee.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.provider.swift.ExportIDL;
import org.thriftee.tests.AbstractThriftEETest;

public class ProcessIDLTest {

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  public ProcessIDLTest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testProcessIDL() throws IOException {
    File temp = ExportIDLTest.getTempDirForTest();
    File[] idlFiles = exportIDL(temp);
    ThriftCommand cmd = new ThriftCommand(Generate.PHP);
    //cmd.setThriftCommand(AbstractThriftEETest.thriftExecutable().getAbsolutePath());
    cmd.addFlag(Flag.PHP_OOP);
    cmd.addFlag(Flag.PHP_NAMESPACE);
    ProcessIDL processor = new ProcessIDL();
    File zipFile = processor.process(idlFiles, temp, "php-library", cmd);
    
    FileInputStream fileIn = null;
    ZipInputStream zipIn = null;
    try {
      fileIn = new FileInputStream(zipFile);
      zipIn = new ZipInputStream(fileIn);
      for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
        LOG.debug("Zip Entry: " + entry.getName());
      }
    } finally {
      FileUtil.forceClosed(zipIn);
      FileUtil.forceClosed(fileIn);
    }
  }

  private File[] exportIDL(File temp) {
    try {
      ExportIDL exporter = new ExportIDL();
      return exporter.export(temp, ExportIDLTest.TEST_CLASSES);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
}
*/