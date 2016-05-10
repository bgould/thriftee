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

import static org.thriftee.core.util.FileUtil.UTF_8;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.core.util.FileUtil;

/**
 * <p>
 *  Generates IDL code for a set of a Swift-annotated classes for use in 
 *  by the regular Thrift compiler.
 * </p>
 * @author bcg
 */
public class ExportIDL {

  protected final Logger logger = LoggerFactory.getLogger(getClass()); 
  
  public ExportIDL() {
  }

  /**
   * <p>
   *  Processes a set of classes and saves the IDL files a specified directory.
   * </p>
   * <p>
   *  Two directories will be created, one called <code>swift</code> and the 
   *  other <code>thrift</code>, depending on the flavor of IDL.
   * </p> 
   * @param basedir Destination directory for the output files. Will be deleted and recreated if it already exists.
   * @param classes A set of Swift-annotated classes to process.
   * @return An array of <code>{@link java.io.File}</code> objects representing the IDL that was generated.
   * @throws IOException
   */
  public File[] export(final File basedir, final Set<Class<?>> classes) throws IOException {    
    
    // Set up the directories where the output from the process will be saved
    if (basedir.exists()) {
      FileUtil.deleteRecursively(basedir);
    }
    final File swiftDir = new File(basedir, "swift");
    final File thriftDir = new File(basedir, "thrift");
    if (!swiftDir.mkdirs() || !thriftDir.mkdirs()) {
      throw new IllegalArgumentException(
        "could not write necessary directories to : " + 
        basedir.getAbsolutePath()
      );
    }

    logger.trace("Creating IDL in temp directory: {}", basedir);
    logger.trace("Generating IDL for classes: {}", classes);
    
    // run the "Swift2Thrift" generator to produce IDL from annotated classes
    final Generator generator = new Generator();
    generator.setTempDir(swiftDir);
    generator.setClasses(classes.toArray(new Class[classes.size()]));
    generator.generate();
    
    // create a single file that includes all of the others
    final StringBuilder global_thrift = new StringBuilder();
    
    // copy to the 'swift' files to 'thrift'
    for (final File swiftFile : thriftFilesIn(swiftDir)) { 
      
      // Read the swift file and replace the swift namespace with standard
      // thrift namespaces
      final String swiftFileStr = FileUtil.readAsString(swiftFile, "UTF-8");
      final Pattern namespacePattern = Pattern.compile("namespace java\\.swift (.+)");
      final Matcher m = namespacePattern.matcher(swiftFileStr);
      final String thriftFileStr;
      if (m.find()) {
        final String namespace = m.group(1);
        final StringBuilder sb = new StringBuilder();
        sb.append("namespace * ").append(namespace).append('\n');
//        sb.append("namespace cpp  ").append(namespace).append('\n');
//        sb.append("namespace d    ").append(namespace).append('\n');
//        sb.append("namespace java ").append(namespace).append('\n');
//        sb.append("namespace php  ").append(namespace).append('\n');
//        sb.append("namespace perl ").append(namespace).append('\n');
        thriftFileStr = m.replaceFirst(sb.toString());
        logger.trace("Rewriting swift namespace: {}", sb);
      } else {
        logger.warn(
          "Could not find swift namespace in file:\n" + 
          "---------------------------------------\n" + 
          swiftFileStr
        );
        thriftFileStr = swiftFileStr;
      }
      
      // Create the modified thrift IDL file and append it to the global include
      final File thriftFile = new File(thriftDir, swiftFile.getName());
      FileUtil.writeStringToFile(thriftFileStr, thriftFile, UTF_8);
      global_thrift.append("include \"" + thriftFile.getName() + "\"\n");
      logger.trace(
        "Swift file `{}` copied to `{}`", 
        swiftFile.getAbsolutePath(), 
        thriftFile.getAbsolutePath()
      );
      
    }
    
    // Write the global include file
    final File globalFile = new File(thriftDir, "global.thrift");
    FileUtil.writeStringToFile(global_thrift.toString(), globalFile, UTF_8);
    
    // Return an array of the files in the directory we just created
    return thriftFilesIn(thriftDir);
    
  }

  /**
   * Filters files in a directory with a <code>.thrift</code> extension
   * @param dir The directory to scan.
   * @return An array of the matching files.
   */
  protected File[] thriftFilesIn(File dir) {
    final FileFilter filter = new FileFilter() {
      private final Pattern matcher = Pattern.compile(".*\\.thrift$");
      @Override
      public boolean accept(File pathname) {
        if (matcher.matcher(pathname.getName()).matches()) {
          return true;
        }
        return false;
      }
    };
    final File[] thriftFiles = dir.listFiles(filter);
    return thriftFiles;
  }
  
}
