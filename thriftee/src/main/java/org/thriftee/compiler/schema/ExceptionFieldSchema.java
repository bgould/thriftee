package org.thriftee.compiler.schema;

import java.util.Collection;

import org.thriftee.compiler.schema.ExceptionSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class ExceptionFieldSchema extends AbstractFieldSchema<ExceptionSchema, ExceptionFieldSchema> {

    private ExceptionFieldSchema(
            ExceptionSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type,
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(
            ExceptionSchema.class, 
            ExceptionFieldSchema.class, 
            _parent, 
            _name, 
            _annotations, 
            _type, 
            _required, 
            _identifier
        );
    }

    private static final long serialVersionUID = 1432035891017906486L;

    public static class Builder extends AbstractFieldSchema.AbstractFieldBuilder<ExceptionSchema, ExceptionFieldSchema, ExceptionSchema.Builder, ExceptionFieldSchema.Builder>  {

        public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
            this(null);
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }
        
        Builder(ExceptionSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected String _fieldTypeName() {
            return "field";
        }

        @Override
        protected ExceptionFieldSchema _buildInstance(ExceptionSchema _parent) throws SchemaBuilderException {
            return new ExceptionFieldSchema(_parent, getName(), getAnnotations(), getType(), isRequired(), getIdentifier());
        }

        @Override
        @ThriftConstructor
        public ExceptionFieldSchema build() throws SchemaBuilderException {
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }

    }
    
}
