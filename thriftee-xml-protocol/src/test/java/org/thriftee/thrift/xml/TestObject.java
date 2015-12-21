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

import java.io.File;

import org.apache.thrift.TBase;

public class TestObject {

  final String name;
  final String module;
  final String struct;
  final TBase<?, ?> obj;

  public TestObject(
      String name,
      String module,
      TBase<?, ?> obj) {
    super();
    this.name = name;
    this.module = module;
    this.struct = obj.getClass().getSimpleName();
    this.obj = obj;
  }

  public File verboseXml() {
    return new File(BaseThriftXMLTest.structDir(), name + "/verbose.xml");
  }

  public File conciseXml() {
    return new File(BaseThriftXMLTest.structDir(), name + "/concise.xml");
  }

  public File streamingXml() {
    return new File(BaseThriftXMLTest.structDir(), name + "/streaming.xml");
  }

  public File simpleXml() {
    return new File(BaseThriftXMLTest.structDir(), name + "/simple.xml");
  }

}