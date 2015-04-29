package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public interface ISchemaValue {

    public Object getValue();

    public ThriftProtocolType getThriftProtocolType();

    public String toThriftIDLString();

}
