package org.thriftee.compiler.schema;

import java.io.Serializable;


public abstract class ContainerSchemaType implements ISchemaType, Serializable {

    private static final long serialVersionUID = 34730300350398087L;

    private final ISchemaType valueType;

    protected ContainerSchemaType(ISchemaType valueType) {
        this.valueType = valueType;
    }

    public ISchemaType getValueType() {
        return this.valueType;
    }

    @Override
    public String getModuleName() {
        return null;
    }

}
