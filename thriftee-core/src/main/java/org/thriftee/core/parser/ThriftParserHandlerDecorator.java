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

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.thriftee.compiler.schema.AbstractFieldSchema;
import org.thriftee.compiler.schema.AbstractStructSchema;
import org.thriftee.compiler.schema.EnumSchema;
import org.thriftee.compiler.schema.MethodSchema;

public class ThriftParserHandlerDecorator implements ThriftParserHandler {

  private final ThriftParserHandler delegate;

  public ThriftParserHandlerDecorator(ThriftParserHandler delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public void onMessageBegin(TMessage msg, MethodSchema schema) throws TException {
    delegate.onMessageBegin(msg, schema);
  }

  @Override
  public void onMessageEnd() throws TException {
    delegate.onMessageEnd();
  }

  @Override
  public void onStructBegin(TStruct struct, AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
    delegate.onStructBegin(struct, schema);
  }

  @Override
  public void onStructEnd() throws TException {
    delegate.onStructEnd();
  }

  @Override
  public void onFieldBegin(TField field, AbstractFieldSchema<?, ?> schema) throws TException {
    delegate.onFieldBegin(field, schema);
  }

  @Override
  public void onFieldEnd() throws TException {
    delegate.onFieldEnd();
  }

  @Override
  public void onFieldStop() throws TException {
    delegate.onFieldStop();
  }

  @Override
  public void onMapBegin(TMap map) throws TException {
    delegate.onMapBegin(map);
  }

  @Override
  public void onMapEnd() throws TException {
    delegate.onMapEnd();
  }

  @Override
  public void onListBegin(TList list) throws TException {
    delegate.onListBegin(list);
  }

  @Override
  public void onListEnd() throws TException {
    delegate.onListEnd();
  }

  @Override
  public void onSetBegin(TSet set) throws TException {
    delegate.onSetBegin(set);
  }

  @Override
  public void onSetEnd() throws TException {
    delegate.onSetEnd();
  }

  @Override
  public void onBool(boolean val) throws TException {
    delegate.onBool(val);
  }

  @Override
  public void onByte(byte val) throws TException {
    delegate.onByte(val);
  }

  @Override
  public void onI16(short val) throws TException {
    delegate.onI16(val);
  }

  @Override
  public void onI32(int val) throws TException {
    delegate.onI32(val);
  }

  @Override
  public void onI64(long val) throws TException {
    delegate.onI64(val);
  }

  @Override
  public void onString(String val) throws TException {
    delegate.onString(val);
  }

  @Override
  public void onBinary(ByteBuffer val) throws TException {
    delegate.onBinary(val);
  }

  @Override
  public void onDouble(double val) throws TException {
    delegate.onDouble(val);
  }

  @Override
  public void onEnum(int val, EnumSchema schema) throws TException {
    delegate.onEnum(val, schema);
  }

}
