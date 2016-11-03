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
package org.thriftee.core.proxy;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.thriftee.core.parser.ThriftParserHandler;
import org.thriftee.thrift.schema.AbstractFieldSchema;
import org.thriftee.thrift.schema.AbstractStructSchema;
import org.thriftee.thrift.schema.EnumSchema;
import org.thriftee.thrift.schema.MethodSchema;

public class TProtocolProxy implements ThriftParserHandler {

  private final TProtocol delegate;

  public TProtocolProxy(final TProtocol delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onMessageBegin(TMessage msg, MethodSchema schema) throws TException {
    delegate.writeMessageBegin(msg);
  }

  @Override
  public void onMessageEnd() throws TException {
    delegate.writeMessageEnd();
  }

  @Override
  public void onStructBegin(TStruct struct, AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
    delegate.writeStructBegin(struct);
  }

  @Override
  public void onStructEnd() throws TException {
    delegate.writeStructEnd();
  }

  @Override
  public void onFieldBegin(TField field, AbstractFieldSchema<?, ?> schema) throws TException {
    delegate.writeFieldBegin(field);
  }

  @Override
  public void onFieldEnd() throws TException {
    delegate.writeFieldEnd();
  }

  @Override
  public void onMapBegin(TMap map) throws TException {
    delegate.writeMapBegin(map);
  }

  @Override
  public void onMapEnd() throws TException {
    delegate.writeMapEnd();
  }

  @Override
  public void onListBegin(TList list) throws TException {
    delegate.writeListBegin(list);
  }

  @Override
  public void onListEnd() throws TException {
    delegate.writeListEnd();
  }

  @Override
  public void onSetBegin(TSet set) throws TException {
    delegate.writeSetBegin(set);
  }

  @Override
  public void onSetEnd() throws TException {
    delegate.writeSetEnd();
  }

  @Override
  public void onBool(boolean val) throws TException {
    delegate.writeBool(val);
  }

  @Override
  public void onByte(byte val) throws TException {
    delegate.writeByte(val);
  }

  @Override
  public void onI16(short val) throws TException {
    delegate.writeI16(val);
  }

  @Override
  public void onI32(int val) throws TException {
    delegate.writeI32(val);
  }

  @Override
  public void onI64(long val) throws TException {
    delegate.writeI64(val);
  }

  @Override
  public void onString(String val) throws TException {
    delegate.writeString(val);
  }

  @Override
  public void onBinary(ByteBuffer val) throws TException {
    delegate.writeBinary(val);
  }

  @Override
  public void onDouble(double val) throws TException {
    delegate.writeDouble(val);
  }

  @Override
  public void onFieldStop() throws TException {
    delegate.writeFieldStop();
  }

  @Override
  public void onEnum(int val, EnumSchema schema) throws TException {
    delegate.writeI32(val);
  }

}
