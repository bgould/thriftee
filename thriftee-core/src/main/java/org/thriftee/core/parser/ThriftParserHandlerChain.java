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
package org.thriftee.core.parser;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.thriftee.thrift.schema.AbstractFieldSchema;
import org.thriftee.thrift.schema.AbstractStructSchema;
import org.thriftee.thrift.schema.EnumSchema;
import org.thriftee.thrift.schema.MethodSchema;

public class ThriftParserHandlerChain implements ThriftParserHandler {

  private final ThriftParserHandler[] listeners;

  public ThriftParserHandlerChain(final ThriftParserHandler... lstnrs) {
    List<ThriftParserHandler> list = new ArrayList<>(lstnrs.length);
    for (final ThriftParserHandler l : lstnrs) {
      if (l != null) {
        list.add(l);
      }
    }
    this.listeners = list.toArray(new ThriftParserHandler[list.size()]);
  }

  @Override
  public void onMessageBegin(TMessage msg, MethodSchema schema) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onMessageBegin(msg, schema);
    }
  }

  @Override
  public void onMessageEnd() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onMessageEnd();
    }
  }

  @Override
  public void onStructBegin(TStruct struct, AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onStructBegin(struct, schema);
    }
  }

  @Override
  public void onStructEnd() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onStructEnd();
    }
  }

  @Override
  public void onFieldBegin(TField field, AbstractFieldSchema<?, ?> schema) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onFieldBegin(field, schema);
    }
  }

  @Override
  public void onFieldEnd() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onFieldEnd();
    }
  }

  @Override
  public void onFieldStop() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onFieldStop();
    }
  }

  @Override
  public void onMapBegin(TMap map) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onMapBegin(map);
    }
  }

  @Override
  public void onMapEnd() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onMapEnd();
    }
  }

  @Override
  public void onListBegin(TList list) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onListBegin(list);
    }
  }

  @Override
  public void onListEnd() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onListEnd();
    }
  }

  @Override
  public void onSetBegin(TSet set) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onSetBegin(set);
    }
  }

  @Override
  public void onSetEnd() throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onSetEnd();
    }
  }

  @Override
  public void onBool(boolean val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onBool(val);
    }
  }

  @Override
  public void onByte(byte val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onByte(val);
    }
  }

  @Override
  public void onI16(short val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onI16(val);
    }
  }

  @Override
  public void onI32(int val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onI32(val);
    }
  }

  @Override
  public void onI64(long val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onI64(val);
    }
  }

  @Override
  public void onString(String val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onString(val);
    }
  }

  @Override
  public void onBinary(ByteBuffer val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onBinary(val);
    }
  }

  @Override
  public void onDouble(double val) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onDouble(val);
    }
  }

  @Override
  public void onEnum(int val, EnumSchema schema) throws TException {
    for (ThriftParserHandler l : listeners) {
      l.onEnum(val, schema);
    }
  }

}
