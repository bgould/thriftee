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