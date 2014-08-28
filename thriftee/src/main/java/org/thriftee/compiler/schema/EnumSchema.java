package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class EnumSchema extends BaseSchemaType<ModuleSchema> {

    private static final long serialVersionUID = -6204420892157052800L;

    private final ModuleSchema module;
    
    private final Map<String, EnumValueSchema> enumValues;
    
    public EnumSchema(ModuleSchema parent, String _name, Collection<EnumValueSchema.Builder> enumValues) 
            throws SchemaBuilderException {
        super(ModuleSchema.class, parent, new ReferenceSchemaType(
            ThriftProtocolType.ENUM, parent.getName(), _name
        ), null);
        this.module = parent;
        this.enumValues = toMap(this, enumValues);
    }
    
    public Map<String, EnumValueSchema> getEnumValues() {
        return this.enumValues;
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
    
    public static class Builder extends AbstractSchemaBuilder<ModuleSchema, EnumSchema, ModuleSchema.Builder, Builder> {

        Builder(ModuleSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }
        
        private List<EnumValueSchema.Builder> enumValues = New.linkedList();
        
        public EnumValueSchema.Builder addEnumValue(String name) {
            EnumValueSchema.Builder result = new EnumValueSchema.Builder(this);
            return result.name(name);
        }

        @Override
        protected EnumSchema _build(ModuleSchema _parent) throws SchemaBuilderException {
            super._validate();
            EnumSchema result = new EnumSchema(_parent, getName(), enumValues);
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "enumValues" };
        }
        
    }
    
}
