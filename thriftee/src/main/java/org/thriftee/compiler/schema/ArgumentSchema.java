package org.thriftee.compiler.schema;

import java.util.Collection;


public class ArgumentSchema extends AbstractFieldSchema<MethodSchema> {

    protected ArgumentSchema(
            MethodSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type, 
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(MethodSchema.class, _parent, _name, _annotations, _type, _required, _identifier);
    }

    private static final long serialVersionUID = 4332069454537397041L;

    public static class Builder extends AbstractFieldBuilder<MethodSchema, ArgumentSchema, MethodSchema.Builder, ArgumentSchema.Builder> {

        protected Builder(MethodSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected String _fieldTypeName() {
            return "argument";
        }

        @Override
        protected ArgumentSchema _buildInstance(MethodSchema _parent) throws SchemaBuilderException {
            return new ArgumentSchema(
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
