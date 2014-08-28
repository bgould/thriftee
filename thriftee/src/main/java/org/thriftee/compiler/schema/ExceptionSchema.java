package org.thriftee.compiler.schema;

import java.util.Collection;

import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class ExceptionSchema extends AbstractStructSchema<ModuleSchema, ExceptionSchema, ExceptionFieldSchema, ExceptionFieldSchema.Builder> {
    
    private static final long serialVersionUID = 9173725847653740446L;
    
    private ExceptionSchema(
            ModuleSchema parent, 
            String _name, 
            Collection<ExceptionFieldSchema.Builder> _fields, 
            Collection<ThriftAnnotation> _annotations
        ) throws SchemaBuilderException {
        super(
            ModuleSchema.class, 
            ExceptionSchema.class,
            parent, 
            _name,
            _fields,
            _annotations
        );
    }
    
    static final class Builder extends AbstractStructSchema.AbstractStructSchemaBuilder<
        ModuleSchema, 
        ExceptionSchema, 
        ModuleSchema.Builder, 
        ExceptionFieldSchema.Builder, 
        ExceptionSchema.Builder> {

        protected Builder(ModuleSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }

        @Override
        protected ExceptionFieldSchema.Builder _createFieldBuilder() {
            return new ExceptionFieldSchema.Builder(this);
        }

        @Override
        protected ExceptionSchema _createStruct(ModuleSchema _parent) throws SchemaBuilderException {
            return new ExceptionSchema(_parent, getName(), _getFields(), getAnnotations());
        }

    }
    
}
