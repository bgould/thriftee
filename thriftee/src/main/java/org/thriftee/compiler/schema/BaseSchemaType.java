package org.thriftee.compiler.schema;

import java.util.Collection;

abstract class BaseSchemaType<P extends BaseSchema<?, ?>, T extends BaseSchema<P, T>> extends BaseSchema<P, T> implements ISchemaType {

    private static final long serialVersionUID = -4797781153586878306L;
    
    private final ReferenceSchemaType reference;
    
    public ReferenceSchemaType getReference() {
        return this.reference;
    }

    protected BaseSchemaType(
            Class<P> parentClass, 
            Class<T> thisClass, 
            P parent, 
            ReferenceSchemaType _reference, 
            Collection<ThriftAnnotation> _annotations) throws SchemaBuilderException {
        super(
            parentClass, 
            thisClass,
            parent, 
            _reference.getTypeName(), 
            (Collection<ThriftAnnotation>) _annotations
        );
        this.reference = _reference;
    }

    @Override
    public String toNamespacedIDL(String namespace) {
        return getReference().toNamespacedIDL(namespace);
    }

}
