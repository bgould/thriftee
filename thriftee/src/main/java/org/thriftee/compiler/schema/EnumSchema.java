package org.thriftee.compiler.schema;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.Strings;

import com.facebook.swift.codec.ThriftProtocolType;

public class EnumSchema extends BaseSchemaType<ModuleSchema> {

    private static final long serialVersionUID = -6204420892157052800L;

    private final ModuleSchema module;
    
    public EnumSchema(ModuleSchema parent, String _name) {
        super(ModuleSchema.class, parent, new ReferenceSchemaType(
            ThriftProtocolType.ENUM, parent.getName(), _name
        ));
        this.module = parent;
    }
    
    public ModuleSchema getModule() {
        return this.module;
    }
    
    public String getModuleName() {
        return this.getModule().getName();
    }
    
    public String getTypeName() {
        return this.getName();
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        return ThriftProtocolType.ENUM;
    }
    
    public static class Builder extends AbstractSchemaBuilder<ModuleSchema, EnumSchema, ModuleSchema.Builder> {

        Builder(ModuleSchema.Builder parentBuilder) {
            super(parentBuilder);
        }

        private String name;

        public Builder name(String _name) {
            this.name = _name;
            return this;
        }

        @Override
        protected EnumSchema _build(ModuleSchema _parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "enum");
            }
            EnumSchema result = new EnumSchema(_parent, this.name);
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name" };
        }
        
    }
    
}
