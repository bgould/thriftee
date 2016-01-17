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

import static org.thriftee.examples.Examples.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.thriftee.thrift.xml.Transformation.RootType;
import org.thriftee.thrift.xml.protocol.TestProtocol;

public class BaseThriftXMLTest {

  @Rule
  public TestName testName = new TestName();

  public static final File testDir = new File("target/tests");

  public static final File testModelDir = new File(testDir, "models");

  public static final File testSchemaDir = new File(testDir, "schemas");

  public static final File testStructsDir = new File(testDir, "structs");

  public static final Transforms Transforms = new Transforms();

  private static Map<String, File> exportedWsdls = null;

  private static Map<String, File> exportedModels = null;

  private static Map<String, File> exportedSchemas = null;

  private static Map<String, TestObject> exportedStructs = null;

  public final String simpleName = getClass().getSimpleName();

  protected final File testClassDir = new File("target/tests/" + simpleName);

  protected File testMethodDir;

  public TestProtocol createOutProtocol(String s) {
    return new TestProtocol(s);
  }

  public TestProtocol createOutProtocol(File file) {
    return createOutProtocol(readFileAsString(file));
  }

  public TestProtocol createOutProtocol() {
    return createOutProtocol((String)null);
  }

  private static final TestObject[] objs = new TestObject[] {

    new TestObject("everything", "everything", everythingStruct()),
    new TestObject("blotto", "nothing_all_at_once", blotto()),

    new TestCall("grok_args",   "everything", "Universe", grokArgs()   ),
    new TestCall("grok_result", "everything", "Universe", grokResult() ),
    new TestCall("grok_error",  "everything", "Universe", grokError()  ),

  };

  @BeforeClass
  public synchronized static void beforeClass() throws Exception {
    if (exportedModels == null || exportedSchemas == null) {
      createDir("schema", testSchemaDir);
      createDir("structs", testStructsDir);
      exportedModels  = exportModels(testModelDir);
      exportedSchemas = exportSchemas(exportedModels, testSchemaDir);
      exportedWsdls   = exportWsdls(exportedModels, testSchemaDir);
      exportedStructs = exportStructs(objs, testStructsDir);
    }
  }

  private static void createDir(String label, File dir) throws IOException {
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
//    if (!exportedModels.containsKey(module)) {
//      throw new IllegalArgumentException("No model file for '" + module + "'.");
//    }
//    return exportedModels.get(module);
    return exportedModels.get("xml_tests");
  }

  public static URL urlToModelFor(String module) throws IOException {
    return modelFor(module).toURI().toURL();
  }

  public static File schemaFor(String module) {
    if (!exportedSchemas.containsKey(module)) {
      throw new IllegalArgumentException("No xsd file for '" + module + "'.");
    }
    return exportedSchemas.get(module);
  }

  public static File wsdlFor(String module) {
    if (!exportedWsdls.containsKey(module)) {
      throw new IllegalArgumentException("No WSDL file for '" + module + "'.");
    }
    return exportedWsdls.get(module);
  }

  public static TestObject structFor(String name) {
    if (!exportedStructs.containsKey(name)) {
      throw new IllegalArgumentException("No struct object for '" + name + "'");
    }
    return exportedStructs.get(name);
  }

  public static File structDir() {
    return testStructsDir;
  }

  public static Map<String, File> exportModels(File tmp) throws IOException {
    final Map<String, File> xmlFiles = new LinkedHashMap<>();
    final File[] idlFiles = new File("src/test/thrift").listFiles(
      new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".thrift");
        }
      }
    );
    if (idlFiles == null) {
      throw new IllegalStateException("No thrift files found in test dir.");
    }
    for (final File idlfile : idlFiles) {
      final String basename = idlfile.getName().replaceAll(".thrift$", "");
      final File outfile = new File(tmp, basename + ".xml");
      if (!outfile.exists()) {
        throw new IOException("could not find generated XML model: " + outfile);
      }
      xmlFiles.put(basename, outfile);
    }
    return Collections.unmodifiableMap(xmlFiles);
  }

  public static Map<String, File> exportSchemas(
      Map<String, File> models, File tmp) throws IOException {
    final Map<String, File> xsdFiles = new TreeMap<>();
    for (Entry<String, File> entry : models.entrySet()) {
      xsdFiles.putAll(Transforms.exportSchemas(entry.getValue(), tmp));
    }
    return Collections.unmodifiableMap(xsdFiles);
  }

  public static Map<String, File> exportWsdls(
      Map<String, File> models, File tmp) throws IOException {
    final Map<String, File> wsdlFiles = new TreeMap<>();
    for (Entry<String, File> entry : models.entrySet()) {
      wsdlFiles.putAll(Transforms.exportWsdls(entry.getValue(), tmp));
    }
    return Collections.unmodifiableMap(wsdlFiles);
  }

  public static Map<String, TestObject> exportStructs(
        final TestObject[] structs, final File tmp
      ) throws IOException, TException, TransformerException {
    final Map<String, TestObject> testobjs = new TreeMap<>();
    for (final TestObject obj : structs) {
      final File dir = new File(tmp, obj.name);
      if (!dir.mkdir()) {
        throw new IOException("could not create directory: " + dir);
      }
      final File simple = new File(dir, "simple.xml");
      final File streaming = new File(dir, "streaming.xml");
      final TestProtocol oprot = new TestProtocol((byte[])null);
      final File outfile = new File(dir, "concise.xml");
      if (obj instanceof TestCall) {
        final TestCall call = (TestCall) obj;
        oprot.writeMessageBegin(new TMessage(call.method, call.type, 1));
        obj.obj.write(oprot);
        oprot.writeMessageEnd();
      } else {
        obj.obj.write(oprot);
      }
      oprot.writeOutputTo(outfile);
      testobjs.put(obj.name, obj);
      transformToSimple(obj, outfile, simple);
      transformToStreaming(obj, simple, streaming);
    }
    return Collections.unmodifiableMap(testobjs);
  }

  private static void transformToSimple(TestObject obj, File src, File tgt)
      throws IOException, TransformerException {
    final Transformation trns = Transforms.newStreamingToSimple();
    trns.setFormatting(true);
    trns.setModelFile(modelFor(obj.module));
    trns.setModule(obj.module);
    if (obj instanceof TestCall) {
      trns.setRoot(RootType.MESSAGE, ((TestCall)obj).service);
    } else {
      trns.setRoot(RootType.STRUCT, obj.struct);
    }
    trns.transform(new StreamSource(src), new StreamResult(tgt));
  }

  private static void transformToStreaming(TestObject obj, File src, File tgt) 
      throws IOException, TransformerException {
    final Transformation trns = Transforms.newSimpleToStreaming();
    trns.setFormatting(true);
    trns.setModelFile(modelFor(obj.module));
    trns.transform(new StreamSource(src), new StreamResult(tgt));
  }

  public static Collection<Object[]> testParameters() {
    final List<Object[]> result = new ArrayList<>();
    for (final TestObject obj : objs) {
      result.add(new Object[] { obj });
    }
    return result;
  }

  public static String readFileAsString(File file) {
    try {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final byte[] buffer = new byte[1024];
      final InputStream in = file.toURI().toURL().openStream();
      for (int n = -1; (n = in.read(buffer)) > -1; ) {
        baos.write(buffer, 0, n);
      }
      return new String(baos.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
