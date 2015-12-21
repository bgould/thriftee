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
package org.thriftee.compiler.schema.template;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.schema.ISchemaType;
import org.thriftee.compiler.schema.MethodArgumentSchema;
import org.thriftee.compiler.schema.MethodIdentifier;
import org.thriftee.compiler.schema.MethodSchema;
import org.thriftee.compiler.schema.StructSchema;
import org.thriftee.framework.ThriftEE;

import com.facebook.swift.codec.internal.TProtocolWriter;

public class TemplateMaker {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final ThriftEE _thrift;

  public TemplateMaker(final ThriftEE thrift) {
    this._thrift = thrift;
  }

  protected ThriftEE thrift() {
    return _thrift;
  }

  public void writeStruct(
      final TProtocol protocol, 
      final StructSchema type
    ) throws TException {
    
  }

  public void writeCall(
      final TProtocol protocol, 
      final MethodIdentifier id
    ) throws TException {

    final MethodSchema method = thrift().schema().findMethod(id);

    final TMessage msg = new TMessage(method.getName(), TMessageType.CALL, 0);
    protocol.writeMessageBegin(msg);

    // write the parameters
    final TProtocolWriter writer = new TProtocolWriter(protocol);
    writer.writeStructBegin(method.getName() + "_args");
    for (int i = 0, c = method.getArguments().size(); i < c; i++) {
      final MethodArgumentSchema argschema = method.getArguments().get(i);
      switch (argschema.getType().getProtocolType()) {
      case UNKNOWN:
        throw new IllegalStateException("argument type should not be unknown.");
      case BOOL:
        writer.writeBool(true);
        break;
      case BYTE:
        writer.writeByte((byte) 42);
        break;
      case DOUBLE:
        writer.writeDouble((double) 1);
        break;
      case I16:
        writer.writeI16((short) 1);
        break;
      case I32:
        writer.writeI32((int) 1);
        break;
      case I64:
        writer.writeI64((long) 1);
        break;
      case BINARY:
        writer.writeBinary(ByteBuffer.wrap("foobar".getBytes()));
      case STRING:
        writer.writeString("?");
      case STRUCT:
        final ISchemaType schemaType = argschema.getType().unwrap();
        if (schemaType instanceof StructSchema) {
          writeStruct(protocol, ((StructSchema) schemaType));
        } else {
          throw new UnsupportedOperationException(
            "don't know how to write " + schemaType
          );
        }
      case MAP:
      case SET:
      case LIST:
      case ENUM:
        throw new UnsupportedOperationException("not supported yet");
      }
    }
    writer.writeStructEnd();
  }

  

}
