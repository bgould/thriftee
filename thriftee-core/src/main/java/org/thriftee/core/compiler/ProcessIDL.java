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
package org.thriftee.core.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.compiler.ExecutionResult;
import org.apache.thrift.compiler.ThriftCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.core.util.FileUtil;

public class ProcessIDL {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final ThriftCompiler compiler;

  private File thriftLibDir;

  private PostProcessor postProcessor;

  public ProcessIDL(final ThriftCompiler compiler) {
    if (compiler == null) {
      throw new IllegalArgumentException("compiler cannot be null");
    }
    this.compiler = compiler;
  }

  public ProcessIDL(
      final ThriftCompiler compiler,
      final File thriftLibDir,
      final PostProcessor postProcessor) {
    this(compiler);
    this.thriftLibDir = thriftLibDir;
    this.postProcessor = postProcessor;
  }

  public File process(
      File[] idlFiles,
      File workDir,
      String zipName,
      ThriftCommand cmd,
      File... extraZipDirectories
  ) throws IOException {
    File outputDir = getOutputDir(workDir, zipName);
    if (outputDir.exists()) {
      FileUtil.deleteRecursively(outputDir);
    }
    if (!outputDir.mkdirs()) {
      throw new IOException(
        "could not create output directory: " + outputDir.getAbsolutePath());
    }
    File zipFile = new File(workDir, zipName + ".zip");
    if (zipFile.exists()) {
      if (!zipFile.delete()) {
        throw new IOException(
          "Could not delete existing zip file: " + zipFile.getAbsolutePath());
      }
    }
    cmd.setOutputLocation(outputDir);
    for (File file : idlFiles) {
      cmd.setThriftFile(file);
      final List<String> args = cmd.command();
      args.remove(0);
      logger.debug("processing thrift IDL: {}", args);
      final String[] argArray = args.toArray(new String[args.size()]);
      final ExecutionResult result = compiler.execute(argArray);
      if (result.exitCode != 0) {
        throw new IOException(
            "thrift generation failed with exit code: " + result.exitCode + "; "
            + "\nstderr: " + result.errString + ";\nstdout: " + result.outString);
      }
      /*
      ProcessBuilder pb = new ProcessBuilder(cmd.command());
      pb.inheritIO();
      Process process = pb.start();
      try {
        int exit = process.waitFor();
        if (exit > 0) {
          throw new IOException(
            "thrift generation failed with exit code: " + exit);
        }
      } catch (InterruptedException e) {
        throw new IOException(
            "Thrift generation process was interrupted.", e);
      }
      */
    }
    final PostProcessor pp = getPostProcessor();
    if (pp != null) {
      logger.trace("executing post processor: {}", pp);
      PostProcessorEvent event = new PostProcessorEvent(
        outputDir,
        thriftLibDir,
        extraZipDirectories != null ? Arrays.asList(extraZipDirectories) : null
      );
      pp.postProcess(event);
    }
    FileUtil.createZipFromDirectory(zipFile, "", outputDir, extraZipDirectories);
    if (extraZipDirectories != null) {
      for (File extra : extraZipDirectories) {
        if (extra != null) {
          final File[] files = extra.listFiles();
          if (files != null) {
            for (File file : files) {
              logger.debug("copying recursively: {}", file.getAbsolutePath());
              FileUtil.copyRecursively(extra, outputDir);
            }
          }
        }
      }
    }
    return zipFile;
  }

  public File getOutputDir(File workDir, String zipName) {
    final File outputDir = new File(workDir, zipName);
    return outputDir;
  }

  /**
   * @return the postProcessor
   */
  public PostProcessor getPostProcessor() {
    return postProcessor;
  }

  /**
   * @param postProcessor the postProcessor to set
   */
  public void setPostProcessor(PostProcessor postProcessor) {
    this.postProcessor = postProcessor;
  }

}
