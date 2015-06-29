package org.thriftee.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.util.FileUtil;

public class ProcessIDL {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private File thriftLibDir;

  private PostProcessor postProcessor;

  public ProcessIDL() {}

  public ProcessIDL(File thriftLibDir, PostProcessor postProcessor) {
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
      logger.debug("processing thrift IDL: {}", cmd.commandString());
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
        for (File file : extra.listFiles()) {
          logger.debug("copying recursively: {}", file.getAbsolutePath());
          FileUtil.copyRecursively(extra, outputDir);
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
