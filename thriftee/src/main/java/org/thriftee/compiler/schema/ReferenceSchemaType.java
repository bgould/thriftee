package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public class ReferenceSchemaType implements ISchemaType {

    private final ThriftProtocolType protocolType;
    
    private final String moduleName;
    
    private final String typeName;
    
    public static ReferenceSchemaType referTo(ThriftProtocolType protocolType, String moduleName, String typeName) {
        return new ReferenceSchemaType(protocolType, moduleName, typeName);
    }
    
    protected ReferenceSchemaType(ThriftProtocolType protocolType, String moduleName, String typeName) {
        super();
        this.protocolType = protocolType;
        this.moduleName = moduleName;
        this.typeName = typeName;
    }

    @Override
    public String getModuleName() {
        return this.moduleName;
    }

    @Override
    public String getTypeName() {
        return this.typeName;
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return this.protocolType;
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        if (namespace != null && getModuleName() != null && namespace.equals(getModuleName())) {
            return getTypeName();
        } else {
            return getModuleName() + "." + getTypeName();
        }
    }

}
