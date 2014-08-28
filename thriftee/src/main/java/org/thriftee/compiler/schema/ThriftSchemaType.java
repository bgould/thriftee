package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.codec.ThriftStruct;

/**
 * <p>Thrift does no support polymorphism, so to represent a type in IDL
 * it is necessary to have a wrapper class that represents all possible types.</p>
 * @author bcg
 */
@ThriftStruct
public final class ThriftSchemaType implements ISchemaType {

    public static final int THRIFT_INDEX_MODULE_NAME = 1;
    
    public static final int THRIFT_INDEX_TYPE_NAME = THRIFT_INDEX_MODULE_NAME + 1;
    
    private final ISchemaType schemaType;
    
    public static ThriftSchemaType wrap(ISchemaType _schemaType) {
        return new ThriftSchemaType(_schemaType);
    }
    
    private ThriftSchemaType(ISchemaType _schemaType) {
        this.schemaType = _schemaType;
    }

    @Override
    @ThriftField(THRIFT_INDEX_MODULE_NAME)
    public String getModuleName() {
        return this.schemaType.getModuleName();
    }

    @Override
    @ThriftField(THRIFT_INDEX_TYPE_NAME)
    public String getTypeName() {
        return this.schemaType.getTypeName();
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return this.schemaType.getProtocolType();
    }

    @Override
    public String toNamespacedIDL(String _namespace) {
        return this.schemaType.toNamespacedIDL(_namespace);
    }
    
    public ISchemaType unwrap() {
        return this.schemaType;
    }
    
}
