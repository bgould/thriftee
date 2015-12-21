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
package org.thriftee.framework.client;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.PostProcessorEvent;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.util.FileUtil;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class JQueryClientTypeAlias extends ClientTypeAlias {

  public static final String CONCAT_FILE_NAME = "client-jquery-all.js";
  
  public static final String CONCAT_FILE_MIN_NAME = "client-jquery-all.min.js";

  public static final String JQUERY_FILE_NAME = "jquery.min.js";
  
  public static final String THRIFTEE_JS_NAME = "thriftee.js";

  public static final String THRIFTEE_ALL_JS = "thriftee-all.js";

  public static final String THRIFTEE_ALL_MIN_JS = "thriftee-all.min.js";
  
  public static final String THRIFT_JS_NAME = "thrift.js";

  public static final String JS_PATH = "META-INF/thriftee/clients/jquery/";

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public JQueryClientTypeAlias() {
    super("jquery", Generate.JS, (String) null, Flag.JS_JQUERY);
  }

  @Override
  public void postProcess(PostProcessorEvent event) throws IOException {

    final File dir = event.getDirectory();
    final File libDir = event.getThriftLibDir();
    LOG.debug("Post processing jQuery client library: {}", dir);
    LOG.debug("libDir: {}", libDir.getAbsolutePath());
    
    final String[] gen = dir.list();
    final String[] all = new String[] { 
      THRIFT_JS_NAME,
      CONCAT_FILE_NAME, 
      THRIFTEE_JS_NAME,
    };
   
    final File thriftSrc = new File(libDir, "js/src/" + THRIFT_JS_NAME);
    FileUtil.copyFile(thriftSrc, new File(dir, THRIFT_JS_NAME));
    copyJs(dir, JQUERY_FILE_NAME);
    copyJs(dir, THRIFTEE_JS_NAME);
    
    concatenate(dir, CONCAT_FILE_NAME, gen);
    compressJs(dir, CONCAT_FILE_NAME, CONCAT_FILE_MIN_NAME);
    
    concatenate(dir, THRIFTEE_ALL_JS, all);
    compressJs(dir, THRIFTEE_ALL_JS, THRIFTEE_ALL_MIN_JS);
  }

  private void concatenate(
      File dir, 
      String outputFilename, 
      String[] dirList) throws IOException {
    final File outputFile = new File(dir, outputFilename);
    if (outputFile.exists()) {
      throw new IOException("output file already exists: " + outputFile);
    }
    LOG.debug("--- writing {} ---", outputFile.getName());
    FileWriter fw = null;
    PrintWriter pw = null;
    try {
      final List<File> files = new ArrayList<>();
      final Set<String> filenames = new LinkedHashSet<>(Arrays.asList(dirList));
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
    LOG.debug("--- finished {} ---", outputFile.getName());
  }

  private File copyJs(File outputDir, String jsFileName) throws IOException {
    LOG.debug("Copying {} into client folder", JS_PATH + jsFileName);
    final ClassLoader cl = getClass().getClassLoader();
    final URL jQueryResource = cl.getResource(JS_PATH + jsFileName);
    final File jQueryFile = new File(outputDir, jsFileName);
    FileUtil.urlToFile(jQueryResource, jQueryFile);
    return jQueryFile;
  }

  private File compressJs(File outputDir, String jsFileName, String jsMinName)
      throws IOException {
    final Compressor compressor = new Compressor();
    final File outFile = new File(outputDir, jsMinName);
    compressor.compress(new File(outputDir, jsFileName), outFile);
    return outFile;
  }

  public static class Compressor {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private int lineBreak = -1;

    private boolean munge = true;

    private boolean verbose = false; //LOG.isDebugEnabled();

    private boolean preserveSemis= false;

    private boolean disableOpts = false;

    public void compress(File inFile, File outFile) throws IOException {
      FileReader in = null;
      FileWriter out = null;
      try {
        in = new FileReader(inFile);
        out = new FileWriter(outFile);
        compress(in, out);
      } finally {
        try { in.close(); } catch (Exception e) {}
        try { out.close(); } catch (Exception e) {}
      }
    }

    public void compress(Reader in, Writer out) throws IOException {
      final ErrorReporter rpt = new CompressorErrorReporter();
      final JavaScriptCompressor cmp = new JavaScriptCompressor(in, rpt);
      cmp.compress(out, lineBreak, munge, verbose, preserveSemis, disableOpts);
    }

    private class CompressorErrorReporter implements ErrorReporter {
      @Override
      public void error(
          String msg, String src, int line, String lineSrc, int offset) {
        LOG.error(buildMsg(msg, src, line, lineSrc, offset));
      }
      @Override
      public EvaluatorException runtimeError(
          String msg, String src, int line, String lineSrc, int offset) {
        return new EvaluatorException(msg, src, line, lineSrc, offset);
      }
      @Override
      public void warning(
          String msg, String src, int line, String lineSrc, int offset) {
        LOG.warn(buildMsg(msg, src, line, lineSrc, offset));
      }
      private String buildMsg(
          String msg, String src, int line, String lineSrc, int offset) {
        final JSONObject json = new JSONObject();
        json.put("source", src);
        json.put("lineSouce", lineSrc);
        json.put("line", line);
        json.put("offset", offset);
        json.put("message", msg);
        return json.toString(2);
      }
    }

  }

}
