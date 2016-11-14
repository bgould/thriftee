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
package org.thriftee.thrift.protocol;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TTransport;

/**
 * <code>TProtocolDecorator</code> forwards all requests to an enclosed
 * <code>TProtocol</code> instance, providing a way to author concise
 * concrete decorator subclasses.  While it has no abstract methods, it
 * is marked abstract as a reminder that by itself, it does not modify
 * the behaviour of the enclosed <code>TProtocol</code>.
 *
 * <p>See p.175 of Design Patterns (by Gamma et al.)</p>
 *
 * @see org.apache.thrift.protocol.TMultiplexedProtocol
 */
public abstract class TProtocolDecorator extends TProtocol {

    protected TProtocol concreteProtocol_;

    public TProtocolDecorator(TTransport transport) {
      super(transport);
      this.concreteProtocol_ = null;
    }

    /**
     * Encloses the specified protocol.
     * @param protocol All operations will be forward to this protocol.  Must be non-null.
     */
    public TProtocolDecorator(TProtocol protocol) {
        super(protocol.getTransport());
        concreteProtocol_ = protocol;
    }

    @Override
    public void writeMessageBegin(TMessage tMessage) throws TException {
        concreteProtocol_.writeMessageBegin(tMessage);
    }

    @Override
    public void writeMessageEnd() throws TException {
        concreteProtocol_.writeMessageEnd();
    }

    @Override
    public void writeStructBegin(TStruct tStruct) throws TException {
        concreteProtocol_.writeStructBegin(tStruct);
    }

    @Override
    public void writeStructEnd() throws TException {
        concreteProtocol_.writeStructEnd();
    }

    @Override
    public void writeFieldBegin(TField tField) throws TException {
        concreteProtocol_.writeFieldBegin(tField);
    }

    @Override
    public void writeFieldEnd() throws TException {
        concreteProtocol_.writeFieldEnd();
    }

    @Override
    public void writeFieldStop() throws TException {
        concreteProtocol_.writeFieldStop();
    }

    @Override
    public void writeMapBegin(TMap tMap) throws TException {
        concreteProtocol_.writeMapBegin(tMap);
    }

    @Override
    public void writeMapEnd() throws TException {
        concreteProtocol_.writeMapEnd();
    }

    @Override
    public void writeListBegin(TList tList) throws TException {
        concreteProtocol_.writeListBegin(tList);
    }

    @Override
    public void writeListEnd() throws TException {
        concreteProtocol_.writeListEnd();
    }

    @Override
    public void writeSetBegin(TSet tSet) throws TException {
        concreteProtocol_.writeSetBegin(tSet);
    }

    @Override
    public void writeSetEnd() throws TException {
        concreteProtocol_.writeSetEnd();
    }

    @Override
    public void writeBool(boolean b) throws TException {
        concreteProtocol_.writeBool(b);
    }

    @Override
    public void writeByte(byte b) throws TException {
        concreteProtocol_.writeByte(b);
    }

    @Override
    public void writeI16(short i) throws TException {
        concreteProtocol_.writeI16(i);
    }

    @Override
    public void writeI32(int i) throws TException {
        concreteProtocol_.writeI32(i);
    }

    @Override
    public void writeI64(long l) throws TException {
        concreteProtocol_.writeI64(l);
    }

    @Override
    public void writeDouble(double v) throws TException {
        concreteProtocol_.writeDouble(v);
    }

    @Override
    public void writeString(String s) throws TException {
        concreteProtocol_.writeString(s);
    }

    @Override
    public void writeBinary(ByteBuffer buf) throws TException {
        concreteProtocol_.writeBinary(buf);
    }

    @Override
    public TMessage readMessageBegin() throws TException {
        return concreteProtocol_.readMessageBegin();
    }

    @Override
    public void readMessageEnd() throws TException {
        concreteProtocol_.readMessageEnd();
    }

    @Override
    public TStruct readStructBegin() throws TException {
        return concreteProtocol_.readStructBegin();
    }

    @Override
    public void readStructEnd() throws TException {
        concreteProtocol_.readStructEnd();
    }

    @Override
    public TField readFieldBegin() throws TException {
        return concreteProtocol_.readFieldBegin();
    }

    @Override
    public void readFieldEnd() throws TException {
        concreteProtocol_.readFieldEnd();
    }

    @Override
    public TMap readMapBegin() throws TException {
        return concreteProtocol_.readMapBegin();
    }

    @Override
    public void readMapEnd() throws TException {
        concreteProtocol_.readMapEnd();
    }

    @Override
    public TList readListBegin() throws TException {
        return concreteProtocol_.readListBegin();
    }

    @Override
    public void readListEnd() throws TException {
        concreteProtocol_.readListEnd();
    }

    @Override
    public TSet readSetBegin() throws TException {
        return concreteProtocol_.readSetBegin();
    }

    @Override
    public void readSetEnd() throws TException {
        concreteProtocol_.readSetEnd();
    }

    @Override
    public boolean readBool() throws TException {
        return concreteProtocol_.readBool();
    }

    @Override
    public byte readByte() throws TException {
        return concreteProtocol_.readByte();
    }

    @Override
    public short readI16() throws TException {
        return concreteProtocol_.readI16();
    }

    @Override
    public int readI32() throws TException {
        return concreteProtocol_.readI32();
    }

    @Override
    public long readI64() throws TException {
        return concreteProtocol_.readI64();
    }

    @Override
    public double readDouble() throws TException {
        return concreteProtocol_.readDouble();
    }

    @Override
    public String readString() throws TException {
        return concreteProtocol_.readString();
    }

    @Override
    public ByteBuffer readBinary() throws TException {
        return concreteProtocol_.readBinary();
    }
}
