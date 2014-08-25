package org.thriftee.compiler.schema;

public abstract class BaseSchemaType<P extends BaseSchema<?>> extends BaseSchema<P> implements ISchemaType {

    private static final long serialVersionUID = -4797781153586878306L;
    
    private final ReferenceSchemaType reference;
    
    public ReferenceSchemaType getReference() {
        return this.reference;
    }

    protected BaseSchemaType(Class<P> parentClass, P parent, ReferenceSchemaType _reference) {
        super(parentClass, parent, _reference.getTypeName());
        this.reference = _reference;
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        return getReference().toNamespacedIDL(namespace);
    }

}
