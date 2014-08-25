package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public class PrimitiveTypeSchema extends AbstractSchemaType {

    private static final long serialVersionUID = 3247650186888985738L;

    public static final PrimitiveTypeSchema VOID    = new PrimitiveTypeSchema(null);
    
    public static final PrimitiveTypeSchema BOOL    = new PrimitiveTypeSchema(ThriftProtocolType.BOOL);
    
    public static final PrimitiveTypeSchema BYTE    = new PrimitiveTypeSchema(ThriftProtocolType.BYTE);
    
    public static final PrimitiveTypeSchema DOUBLE  = new PrimitiveTypeSchema(ThriftProtocolType.DOUBLE);
    
    public static final PrimitiveTypeSchema I16     = new PrimitiveTypeSchema(ThriftProtocolType.I16);
    
    public static final PrimitiveTypeSchema I32     = new PrimitiveTypeSchema(ThriftProtocolType.I32);
    
    public static final PrimitiveTypeSchema I64     = new PrimitiveTypeSchema(ThriftProtocolType.I64);
    
    public static final PrimitiveTypeSchema LIST    = new PrimitiveTypeSchema(ThriftProtocolType.LIST);
    
    public static final PrimitiveTypeSchema MAP     = new PrimitiveTypeSchema(ThriftProtocolType.MAP);
    
    public static final PrimitiveTypeSchema SET     = new PrimitiveTypeSchema(ThriftProtocolType.SET);
    
    public static final PrimitiveTypeSchema STRING  = new PrimitiveTypeSchema(ThriftProtocolType.STRING);
    
    protected PrimitiveTypeSchema(ThriftProtocolType _protocolType) {
        super(_protocolType);
    }

    public String getModuleName() {
        return null;
    }
    
    public String getTypeName() {
        return getProtocolType().name().toLowerCase();
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        return getTypeName();
    }
    
}
