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
package org.thriftee.thrift.xml;

import static org.thriftee.examples.Examples.blotto;
import static org.thriftee.examples.Examples.everythingStruct;
import static org.thriftee.examples.Examples.grokArgs;
import static org.thriftee.examples.Examples.grokError;
import static org.thriftee.examples.Examples.grokResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TBase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.thrift.compiler.ExecutionResult;
import org.thriftee.thrift.compiler.ThriftCompiler;

import another.Blotto;

public class BaseThriftProtocolTest {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @Rule
  public TestName testName = new TestName();

  public static final File testDir = new File("target/tests");

  public static final File testIdlDir = new File(testDir, "idl");

  public static final File testModelDir = new File(testDir, "models");

  protected static Map<String, File> exportedModels = null;

  public final String simpleName = getClass().getSimpleName();

  protected final File testClassDir = new File("target/tests/" + simpleName);

  protected File testMethodDir;

  protected static final TestObject[] objs = new TestObject[] {

    new TestObject("everything", "everything", everythingStruct()),
    new TestObject("blotto", "nothing_all_at_once", blotto()),
    new TestObject("control_chars", "nothing_all_at_once", controlChars()),

    new TestCall("grok_args",   "everything", "Universe", grokArgs()   ),
    new TestCall("grok_result", "everything", "Universe", grokResult() ),
    new TestCall("grok_error",  "everything", "Universe", grokError()  ),

  };

  public static TBase<?,?> controlChars() {
    final Blotto blotto = blotto();
    blotto.sparticle = "has some control chars: \1";
    return blotto;
  }

  @BeforeClass
  public synchronized static void beforeClass() throws Exception {
    if (exportedModels == null) {
      createDir("idl", testIdlDir);
      createDir("models", testModelDir);
      copyResource("everything.thrift", testIdlDir);
      copyResource("nothing_all_at_once.thrift", testIdlDir);
      exportedModels  = exportModels(testModelDir);
    }
  }

  protected static void createDir(String label, File dir) throws IOException {
    if (dir.exists()) {
      deleteRecursively(dir);
    }
    if (!dir.mkdirs()) {
      throw new IOException("could not create " + label + "dir: " + dir);
    }
  }

  @Before
  public void createTestDir() throws IOException {
    if (!testClassDir.exists()) {
      if (!testClassDir.mkdirs()) {
        throw new IOException("could not create test dir: " + testClassDir);
      }
    }
    this.testMethodDir = new File(
      testClassDir,
      testName.getMethodName().replaceAll("[^a-zA-Z0-9]", "_")
    );
    if (testMethodDir.exists()) {
      deleteRecursively(testMethodDir);
    }
    if (!testMethodDir.mkdirs()) {
      throw new IOException("could not create directory: " + testMethodDir);
    }
  }

  public static void deleteRecursively(File file) throws IOException {
    if (!file.exists()) {
      return;
    }
    if (file.isFile()) {
      if (!file.delete()) {
        throw new IOException("could not delete file: " + file);
      }
      return;
    }
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        for (File dirfile : files) {
          deleteRecursively(dirfile);
        }
        if (!file.delete()) {
          throw new IOException("could not remove directory: " + file);
        }
      }
      return;
    }
    throw new IllegalStateException();
  }

  public static File modelFor(String module) {
    if (!exportedModels.containsKey(module)) {
      throw new IllegalArgumentException("No model file for '" + module + "'.");
    }
    return exportedModels.get(module);
    //return exportedModels.get("xml_tests");
  }

  public static URL urlToModelFor(String module) throws IOException {
    return modelFor(module).toURI().toURL();
  }

  private static void copyResource(String rsrc, File dir) throws IOException {
    final URL url = BaseThriftXMLTest.class.getClassLoader().getResource(rsrc);
    if (url == null) {
      throw new IllegalArgumentException("resource not found: " + rsrc);
    }
    final File file = new File(dir, rsrc);
    try (final FileOutputStream out = new FileOutputStream(file)) {
      try (final InputStream in = url.openStream()) {
        final byte[] buffer = new byte[1024];
        for (int n = -1; (n = in.read(buffer)) > -1; ) {
          out.write(buffer, 0, n);
        }
      }
    }
  }

  public static Map<String, File> exportModels(File tmp) throws IOException {
    final Map<String, File> xmlFiles = new LinkedHashMap<>();
    final File[] idlFiles = testIdlDir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".thrift");
      }
    });
    if (idlFiles == null) {
      throw new IllegalStateException("No thrift files found in test dir.");
    }
    for (final File idlfile : idlFiles) {
      final String basename = idlfile.getName().replaceAll(".thrift$", "");
      final File outfile = new File(tmp, basename + ".xml");
      final ExecutionResult exec = ThriftCompiler.newCompiler().execute(
        "-gen", "xml:merge",
        "-out", tmp.getAbsolutePath(),
        idlfile.getAbsolutePath()
      );
      if (exec.exitCode != 0) {
        throw new IOException(String.format(
          "Unexpected exit code: %s%nstderr:%s%nstdout:%s%n",
          exec.exitCode, exec.errString, exec.outString
        ));
      }
      if (!outfile.exists()) {
        throw new IOException("could not find generated XML model: " + outfile);
      }
      xmlFiles.put(basename, outfile);
    }
    return Collections.unmodifiableMap(xmlFiles);
  }

  public static Collection<Object[]> testParameters() {
    final List<Object[]> result = new ArrayList<>();
    for (final TestObject obj : objs) {
      result.add(new Object[] { obj });
    }
    return result;
  }

}
