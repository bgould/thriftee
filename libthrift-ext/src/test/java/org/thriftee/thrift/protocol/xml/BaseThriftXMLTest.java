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
package org.thriftee.thrift.protocol.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.junit.BeforeClass;
import org.thriftee.thrift.protocol.TestProtocol;
import org.thriftee.thrift.protocol.xml.Transformation;
import org.thriftee.thrift.protocol.xml.Transforms;
import org.thriftee.thrift.protocol.xml.Transformation.RootType;

public class BaseThriftXMLTest extends BaseThriftProtocolTest {

  public static final File testSchemaDir = new File(testDir, "schemas");

  public static final File testStructsDir = new File(testDir, "structs");

  public static final Transforms Transforms = new Transforms();

  private static Map<String, File> exportedWsdls = null;

  private static Map<String, File> exportedSchemas = null;

  private static Map<String, TestObject> exportedStructs = null;

  public TestProtocol createOutProtocol(String s) {
    return new TestProtocol(s);
  }

  public TestProtocol createOutProtocol(File file) {
    return createOutProtocol(readFileAsString(file));
  }

  public TestProtocol createOutProtocol() {
    return createOutProtocol((String)null);
  }

  @BeforeClass
  public synchronized static void beforeClass() throws Exception {
    if (exportedModels == null) {
      BaseThriftProtocolTest.beforeClass();
    }
    if (exportedSchemas == null) {
      createDir("schema", testSchemaDir);
      createDir("structs", testStructsDir);
      exportedSchemas = exportSchemas(exportedModels, testSchemaDir);
      exportedWsdls   = exportWsdls(exportedModels, testSchemaDir);
      exportedStructs = exportStructs(objs, testStructsDir);
    }
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
