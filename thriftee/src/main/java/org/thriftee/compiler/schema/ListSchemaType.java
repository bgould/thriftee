package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftProtocolType;

public class ListSchemaType extends ContainerSchemaType {

    protected ListSchemaType(ISchemaType valueType) {
        super(valueType);
    }

    private static final long serialVersionUID = 1896674969956124265L;

    @Override
    public String getTypeName() {
        return toNamespacedIDL(null);
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return ThriftProtocolType.LIST;
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        return "list<" + getValueType().toNamespacedIDL(namespace) + ">";
    }

}
