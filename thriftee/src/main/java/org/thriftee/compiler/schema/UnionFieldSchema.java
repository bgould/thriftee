package org.thriftee.compiler.schema;

import java.util.Collection;

import com.facebook.swift.codec.ThriftUnion;

@ThriftUnion
public final class UnionFieldSchema extends AbstractFieldSchema<UnionSchema> {

    private UnionFieldSchema(
            UnionSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type,
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(UnionSchema.class, _parent, _name, _annotations, _type, _required, _identifier);
    }

    private static final long serialVersionUID = 1432035891017906486L;

    public static class Builder extends AbstractFieldSchema.AbstractFieldBuilder<UnionSchema, UnionFieldSchema, UnionSchema.Builder, UnionFieldSchema.Builder>  {

        Builder(UnionSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected String _fieldTypeName() {
            return "field";
        }

        @Override
        protected UnionFieldSchema _buildInstance(UnionSchema _parent) throws SchemaBuilderException {
            return new UnionFieldSchema(_parent, getName(), getAnnotations(), getType(), isRequired(), getIdentifier());
        }

    }
    
}
