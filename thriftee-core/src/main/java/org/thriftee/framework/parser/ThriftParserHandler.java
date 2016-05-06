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

public interface ThriftParserHandler {

  void onMessageBegin(TMessage msg, MethodSchema method) throws TException;
  void onMessageEnd() throws TException;
  void onStructBegin(TStruct struct, AbstractStructSchema<?, ?, ?, ?> schema) throws TException;
  void onStructEnd() throws TException;
  void onFieldBegin(TField field, AbstractFieldSchema<?, ?> schema) throws TException;
  void onFieldEnd() throws TException;
  void onFieldStop() throws TException;
  void onMapBegin(TMap map) throws TException;
  void onMapEnd() throws TException;
  void onListBegin(TList list) throws TException;
  void onListEnd() throws TException;
  void onSetBegin(TSet set) throws TException;
  void onSetEnd() throws TException;
  void onBool(boolean val) throws TException;
  void onByte(byte val) throws TException;
  void onI16(short val) throws TException;
  void onI32(int val) throws TException;
  void onI64(long val) throws TException;
  void onString(String val) throws TException;
  void onBinary(ByteBuffer val) throws TException;
  void onDouble(double val) throws TException;
  void onEnum(int val, EnumSchema schema) throws TException;

}
