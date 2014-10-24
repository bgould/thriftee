package org.thriftee.compiler.schema;

import java.util.Collection;

import org.thriftee.compiler.schema.MethodArgumentSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class MethodArgumentSchema extends AbstractFieldSchema<MethodSchema, MethodArgumentSchema> {

    protected MethodArgumentSchema(
            MethodSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type, 
            Requiredness _required, 
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
        
        public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
            this(null);
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }

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
                getRequiredness(), 
                getIdentifier()
            );
        }

        @Override
        @ThriftConstructor
        public MethodArgumentSchema build() throws SchemaBuilderException {
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }

    }
    
}
