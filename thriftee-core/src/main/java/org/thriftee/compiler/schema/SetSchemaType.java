package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public class SetSchemaType extends ContainerSchemaType {

    private static final long serialVersionUID = 7582879752786630514L;

    public SetSchemaType(ISchemaType valueType) {
        super(valueType);
    }

    @Override
    public String getTypeName() {
        return toNamespacedIDL(null);
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return ThriftProtocolType.SET;
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        return "set<" + getValueType().toNamespacedIDL(namespace) + ">";
    }

}
