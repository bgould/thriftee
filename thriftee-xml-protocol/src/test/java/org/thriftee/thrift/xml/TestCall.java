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
