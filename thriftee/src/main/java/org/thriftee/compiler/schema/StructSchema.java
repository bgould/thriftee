package org.thriftee.compiler.schema;

import java.util.Collection;

import org.thriftee.compiler.schema.StructSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class StructSchema extends AbstractStructSchema<ModuleSchema, StructSchema, StructFieldSchema, StructFieldSchema.Builder> {
    
    private static final long serialVersionUID = 9173725847653740446L;
    
    private StructSchema(
            ModuleSchema parent, 
            String _name, 
            Collection<StructFieldSchema.Builder> _fields, 
            Collection<ThriftAnnotation> _annotations
        ) throws SchemaBuilderException {
        super(
            ModuleSchema.class, 
            StructSchema.class,
            parent, 
            _name,
            _fields,
            _annotations
        );
    }
    
    static final class Builder extends AbstractStructSchema.AbstractStructSchemaBuilder<
        ModuleSchema, 
        StructSchema, 
        ModuleSchema.Builder, 
        StructFieldSchema.Builder, 
        StructSchema.Builder> {
        
        public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
            this(null);
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }

        protected Builder(ModuleSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected StructFieldSchema.Builder _createFieldBuilder() {
            return new StructFieldSchema.Builder(this);
        }

        @Override
        protected StructSchema _createStruct(ModuleSchema _parent) throws SchemaBuilderException {
            return new StructSchema(_parent, getName(), _getFields(), getAnnotations());
        }
        
        @Override
        @ThriftConstructor
        public StructSchema build() throws SchemaBuilderException {
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }

    }
    
}
