package org.thriftee.framework.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.PostProcessorEvent;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.util.FileUtil;

public class JQueryClientTypeAlias extends ClientTypeAlias {

  public static final String CONCAT_FILE_NAME = "client-jquery-all.js";

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public JQueryClientTypeAlias() {
    super("jquery", Generate.JS, "js/src", Flag.JS_JQUERY);
  }

  @Override
  public void postProcess(PostProcessorEvent event) throws IOException {
    final File dir = event.getDirectory();
    LOG.debug("Post processing jQuery client library: {}", dir);
    LOG.debug("-------------------------------------");
    final File outputFile = new File(dir, CONCAT_FILE_NAME);
    if (outputFile.exists()) {
      throw new IOException("output file already exists: " + outputFile);
    }
    FileWriter fw = null; // new FileWriter(outputFile);
    PrintWriter pw = null; // new PrintWriter(fw);
    try {
      final List<File> files = new ArrayList<>();
      final Set<String> filenames = new TreeSet<>(Arrays.asList(dir.list()));
      for (final String filename : filenames) {
        final File file = new File(dir, filename);
        files.add(file);
      }
      fw = new FileWriter(outputFile);
      pw = new PrintWriter(fw);
      for (final File file : files) {
        LOG.debug("  {}", file.getName());
        final String content = FileUtil.readAsString(file);
        pw.println(content);
        pw.flush();
      }
    } finally {
      try { pw.close(); } catch (Exception e) {}
      try { fw.close(); } catch (Exception e) {}
    }
    LOG.debug("-------------------------------------");
    LOG.debug("Finished writing concatenated file to: {}", outputFile);
  }

}
