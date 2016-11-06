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
package org.thriftee.thrift.schema;

import java.io.File;

import org.apache.thrift.compiler.ExecutionResult;
import org.apache.thrift.compiler.ThriftCompiler;
import org.thriftee.thrift.xml.BaseThriftProtocolTest;

public class BaseSchemaTest extends BaseThriftProtocolTest {

  public File createXmlModel() {
    final String outdir = testMethodDir.getAbsolutePath();
    final ExecutionResult result = ThriftCompiler.newCompiler().execute(
      "-gen", "xml:merge", "-out", outdir, "target/tests/idl/everything.thrift"
    );
    if (result.successful()) {
      return new File(outdir, "everything.xml");
    } else {
      throw new RuntimeException("thrift exec was not successful: " + result);
    }
  }

}
