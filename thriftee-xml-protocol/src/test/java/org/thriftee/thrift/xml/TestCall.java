package org.thriftee.thrift.xml;

import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TMessageType;

public class TestCall extends TestObject {

  final String service;

  final String method;

  final byte type;

  public TestCall(
        final String name, 
        final String module, 
        final String service, 
        final TBase<?, ?> obj
      ) {
    super(name, module, obj);
    this.service = service;
    if (struct.endsWith("_args")) {
      this.method = struct.substring(0, struct.length() - 5);
      this.type = TMessageType.CALL;
    } else if (struct.endsWith("_result")) {
      this.method = struct.substring(0, struct.length() - 7);
      this.type = TMessageType.REPLY;
    } else {
      throw new IllegalArgumentException(struct);
    }
  }

}
