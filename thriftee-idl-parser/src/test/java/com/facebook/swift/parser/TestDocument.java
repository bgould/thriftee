/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.swift.parser;

import static com.facebook.swift.parser.ThriftIdlParser.*;
import static com.facebook.swift.parser.TreePrinter.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.testng.annotations.Test;

public class TestDocument {

  @Test
  public void testEmpty() throws Exception {
    parseThriftIdl(new StringReader(""));
  }

  @Test
  public void testDocumentFb303() throws Exception {
    System.out.println(treeToString(parseTree(resourceReader("fb303.thrift"))));
    System.out.println(parseThriftIdl(resourceReader("fb303.thrift")));
  }

  @Test
  public void testDocumentHbase() throws Exception {
    System.out.println(treeToString(parseTree(resourceReader("Hbase.thrift"))));
    System.out.println(parseThriftIdl(resourceReader("Hbase.thrift")));
  }

  private static Reader resourceReader(String name) {
    final URL rsrc = TestDocument.class.getClassLoader().getResource(name);
    try (final InputStream in = rsrc.openStream()) {
      final byte[] buffer = new byte[1024];
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      for (int n = -1; (n = in.read(buffer)) > -1; ) {
        baos.write(buffer, 0, n);
      }
      in.close();
      return new StringReader(new String(baos.toByteArray()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
