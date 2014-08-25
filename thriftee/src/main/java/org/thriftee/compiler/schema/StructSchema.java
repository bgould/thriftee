package org.thriftee.compiler.schema;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.Strings;

import com.facebook.swift.codec.ThriftProtocolType;

public class StructSchema extends BaseSchemaType<ModuleSchema> {

    private static final long serialVersionUID = 9173725847653740446L;

    private final ModuleSchema module;
    
    public StructSchema(ModuleSchema parent, String _name) {
        super(ModuleSchema.class, parent, new ReferenceSchemaType(
            ThriftProtocolType.STRUCT, parent.getName(), _name
        ));
        this.module = parent;
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return ThriftProtocolType.STRUCT;
    }
    
    public ModuleSchema getModule() {
        return this.module;
    }
    
    @Override
    public String getModuleName() {
        return getModule().getName();
    }

    @Override
    public String getTypeName() {
        return getName();
    }

    public static class Builder extends AbstractSchemaBuilder<ModuleSchema, StructSchema, ModuleSchema.Builder> {

        Builder(ModuleSchema.Builder parentBuilder) {
            super(parentBuilder);
        }

        private String name;

        public Builder name(String _name) {
            this.name = _name;
            return this;
        }

        @Override
        protected StructSchema _build(ModuleSchema _parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "struct");
            }
            StructSchema result = new StructSchema(_parent, this.name);
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name" };
        }
        
    }
    
}
