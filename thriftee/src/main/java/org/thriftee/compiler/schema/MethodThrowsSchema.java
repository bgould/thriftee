package org.thriftee.compiler.schema;

import java.util.Collection;


public class MethodThrowsSchema extends AbstractFieldSchema<MethodSchema, MethodThrowsSchema> {

    protected MethodThrowsSchema(
            MethodSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type, 
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(
            MethodSchema.class, 
            MethodThrowsSchema.class, 
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
            MethodThrowsSchema, 
            MethodSchema.Builder, 
            MethodThrowsSchema.Builder
        > {

        protected Builder(MethodSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected String _fieldTypeName() {
            return "thrown exception";
        }

        @Override
        protected MethodThrowsSchema _buildInstance(MethodSchema _parent) throws SchemaBuilderException {
            return new MethodThrowsSchema(
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
