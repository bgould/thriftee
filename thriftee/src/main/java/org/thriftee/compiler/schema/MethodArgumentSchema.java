package org.thriftee.compiler.schema;

import java.util.Collection;


public class MethodArgumentSchema extends AbstractFieldSchema<MethodSchema, MethodArgumentSchema> {

    protected MethodArgumentSchema(
            MethodSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type, 
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(
            MethodSchema.class, 
            MethodArgumentSchema.class, 
            _parent, 
            _name, 
            _annotations, 
            _type, 
            _required, 
            _identifier
        );
    }

    private static final long serialVersionUID = 4332069454537397041L;

    public static class Builder extends AbstractFieldBuilder<
            MethodSchema, 
            MethodArgumentSchema, 
            MethodSchema.Builder, 
            MethodArgumentSchema.Builder
        > {

        protected Builder(MethodSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected String _fieldTypeName() {
            return "argument";
        }

        @Override
        protected MethodArgumentSchema _buildInstance(MethodSchema _parent) throws SchemaBuilderException {
            return new MethodArgumentSchema(
                _parent, 
                getName(), 
                getAnnotations(), 
                getType(), 
                isRequired(), 
                getIdentifier()
            );
        }

    }
    
}
