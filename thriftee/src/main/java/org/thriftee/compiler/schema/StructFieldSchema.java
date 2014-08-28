package org.thriftee.compiler.schema;

import java.util.Collection;

import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class StructFieldSchema extends AbstractFieldSchema<StructSchema> {

    private StructFieldSchema(
            StructSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type,
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(StructSchema.class, _parent, _name, _annotations, _type, _required, _identifier);
    }

    private static final long serialVersionUID = 1432035891017906486L;

    public static class Builder extends AbstractFieldSchema.AbstractFieldBuilder<StructSchema, StructFieldSchema, StructSchema.Builder, StructFieldSchema.Builder>  {

        Builder(StructSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected String _fieldTypeName() {
            return "field";
        }

        @Override
        protected StructFieldSchema _buildInstance(StructSchema _parent) throws SchemaBuilderException {
            return new StructFieldSchema(_parent, getName(), getAnnotations(), getType(), isRequired(), getIdentifier());
        }

    }
    
}
