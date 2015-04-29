package org.thriftee.compiler.schema;

import java.io.Serializable;

import com.facebook.swift.codec.ThriftProtocolType;

public abstract class AbstractSchemaType implements ISchemaType, Serializable {

    private static final long serialVersionUID = -5679521951379802595L;

    private final ThriftProtocolType protocolType;

    protected AbstractSchemaType(ThriftProtocolType _protocolType) {
        this.protocolType = _protocolType;
    }
    
    @Override
    public ThriftProtocolType getProtocolType() {
        return this.protocolType;
    }
    
    public boolean isPrimitive() {
        return Utils.isPrimitive(this);
    }

}
