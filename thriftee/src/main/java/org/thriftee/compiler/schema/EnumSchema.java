package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.compiler.schema.EnumSchema.Builder;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class EnumSchema extends BaseSchemaType<ModuleSchema, EnumSchema> {

    public static final int THRIFT_INDEX_NAME = 1;
    
    public static final int THRIFT_INDEX_VALUES = THRIFT_INDEX_NAME + 1;
    
    private static final long serialVersionUID = -6204420892157052800L;
    
    private final Map<String, EnumValueSchema> enumValues;

    public EnumSchema(ModuleSchema parent, String _name, Collection<EnumValueSchema.Builder> enumValues) 
            throws SchemaBuilderException {
        super(ModuleSchema.class, EnumSchema.class, parent, new ReferenceSchemaType(
            ThriftProtocolType.ENUM, parent.getName(), _name
        ), null);
        this.enumValues = toMap(this, enumValues);
    }
    
    @ThriftField(THRIFT_INDEX_VALUES)
    public Map<String, EnumValueSchema> getEnumValues() {
        return this.enumValues;
    }
    
    @Override
    public String getModuleName() {
        return this.getParent().getName();
    }
    
    @Override
    public String getTypeName() {
        return this.getName();
    }
    
    @Override
    @ThriftField(THRIFT_INDEX_NAME)
    public String getName() {
        return super.getName();
    }
    
    @Override
    public ThriftProtocolType getProtocolType() {
        return ThriftProtocolType.ENUM;
    }
    
    public static class Builder extends AbstractSchemaBuilder<ModuleSchema, EnumSchema, ModuleSchema.Builder, Builder> {

        public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
            this(null);
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }
        
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

        @Override
        @ThriftConstructor
        public EnumSchema build() throws SchemaBuilderException {
            throw new NoArgConstructorOnlyExistsForSwiftValidationException();
        }
        
    }
    
}
