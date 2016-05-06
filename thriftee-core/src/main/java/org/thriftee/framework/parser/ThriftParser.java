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
package org.thriftee.framework.parser;

import static org.apache.thrift.protocol.TType.STOP;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.thriftee.compiler.schema.AbstractFieldSchema;
import org.thriftee.compiler.schema.AbstractStructSchema;
import org.thriftee.compiler.schema.EnumSchema;
import org.thriftee.compiler.schema.ISchemaType;
import org.thriftee.compiler.schema.ListSchemaType;
import org.thriftee.compiler.schema.MapSchemaType;
import org.thriftee.compiler.schema.MethodSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.schema.SetSchemaType;
import org.thriftee.compiler.schema.ThriftSchema;

public class ThriftParser {

  private final ThriftSchema schema;

  private final ThriftParserHandler listener;

  public ThriftParser(
      final ThriftSchema schema,
      final ThriftParserHandler listener) {
    if (schema == null) {
      throw new IllegalArgumentException("schema cannot be null");
    }
    if (listener == null) {
      throw new IllegalArgumentException("listener cannot be null");
    }
    this.schema = schema;
    this.listener = listener;
  }

  public void readMessage(ServiceSchema svc, TProtocol in) throws TException {
    final AbstractStructSchema<?, ?, ?, ?> struct;
    {
      final TMessage msg = in.readMessageBegin();
      final MethodSchema method = svc.getMethods().get(msg.name);
      if (method == null) {
        throw new TException(String.format(
          "method '%s' not found{ on %s", msg.name, svc
        ));
      }
      switch (msg.type) {
      case TMessageType.CALL:
        struct = method.getArguments();
        break;
      case TMessageType.REPLY:
        throw new TException("reply not supported yet");
      case TMessageType.EXCEPTION:
        throw new TException("exception not supported yet");
      case TMessageType.ONEWAY:
        throw new TException("oneway not supported yet");
      default:
        throw new TException("unknown message type: " + msg.type);
      }
      listener.onMessageBegin(msg, method);
    }
    readStruct(struct, in);
    in.readMessageEnd();
    listener.onMessageEnd();
  }

  public void readStruct(AbstractStructSchema<?, ?, ?, ?> struct, TProtocol in)
      throws TException {
    {
      final TStruct tstruct = in.readStructBegin();
      listener.onStructBegin(tstruct, struct);
      while (true) {
        final TField tfield = in.readFieldBegin();
        if (tfield.type == STOP) {
          break;
        }
        final AbstractFieldSchema<?, ?> field = struct.getField(tfield.id);
        if (field == null) {
          TProtocolUtil.skip(in, tfield.type);
        } else {
          final ISchemaType type = field.getType();
          if (type.getProtocolType().getType() != tfield.type) {
            TProtocolUtil.skip(in, tfield.type);
          } else {
            listener.onFieldBegin(tfield, field);
            readValue(type, in);
            in.readFieldEnd();
            listener.onFieldEnd();
          }
        }
      }
    }
    listener.onFieldStop();
    in.readStructEnd();
    listener.onStructEnd();
  }

  public void readMap(MapSchemaType type, TProtocol in) throws TException {
    final TMap map = in.readMapBegin();
    listener.onMapBegin(map);
    for (int i = 0; i < map.size; i++) {
      readValue(type.getKeyType(), in);
      readValue(type.getValueType(), in);
//      readValue(schema.resolveType(type.getKeyType()), in);
//      readValue(schema.resolveType(type.getValueType()), in);
    }
    in.readMapEnd();
    listener.onMapEnd();
  }

  public void readSet(SetSchemaType type, TProtocol in) throws TException {
    final TSet set = in.readSetBegin();
    listener.onSetBegin(set);
    final ISchemaType resolved = type.getValueType();
    //final ISchemaType resolved = schema.resolveType(type.getValueType());
    for (int i = 0; i < set.size; i++) {
      readValue(resolved, in);
    }
    in.readSetEnd();
    listener.onSetEnd();
  }

  public void readList(ListSchemaType type, TProtocol in) throws TException {
    final TList list = in.readListBegin();
    listener.onListBegin(list);
    final ISchemaType resolved = type.getValueType();
    //final ISchemaType resolved = schema.resolveType(type.getValueType());
    for (int i = 0; i < list.size; i++) {
      readValue(resolved, in);
    }
    in.readListEnd();
    listener.onListEnd();
  }

  public void readValue(
      final ISchemaType type, 
      final TProtocol in) throws TException {
    switch (type.getProtocolType()) {
    case BOOL: {
      final boolean val = in.readBool();
      listener.onBool(val);
      break;
    }
    case BYTE: {
      final byte val = in.readByte();
      listener.onByte(val);
      break;
    }
    case DOUBLE: {
      final double val = in.readDouble();
      listener.onDouble(val);
      break;
    }
    case I16: {
      final short val = in.readI16();
      listener.onI16(val);
      break;
    }
    case I32: {
      final int val = in.readI32();
      listener.onI32(val);
      break;
    }
    case I64: {
      final long val = in.readI64();
      listener.onI64(val);
      break;
    }
    case STRING: {
      final String val = in.readString();
      listener.onString(val);
      break;
    }
    case BINARY: {
      final ByteBuffer val = in.readBinary();
      listener.onBinary(val);
      break;
    }
    case STRUCT: {
      readStruct(schema.structSchemaFor(type), in);
      break;
    }
    case MAP: {
      readMap(type.castTo(MapSchemaType.class), in);
      break;
    }
    case SET: {
      readSet(type.castTo(SetSchemaType.class), in);
      break;
    }
    case LIST: {
      readList(type.castTo(ListSchemaType.class), in);
      break;
    }
    case ENUM: {
      final int val = in.readI32();
      listener.onEnum(val, type.castTo(EnumSchema.class));
      break;
    }
    default:
      throw new TException("unexpected value type: " + type);
    }
  }

}
