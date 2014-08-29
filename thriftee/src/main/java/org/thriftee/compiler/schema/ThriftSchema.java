package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.compiler.schema.ThriftSchema.Builder;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class ThriftSchema extends BaseSchema<ThriftSchema, ThriftSchema> {

    public static final int THRIFT_INDEX_NAME = 1;
    
    public static final int THRIFT_INDEX_MODULES = THRIFT_INDEX_NAME + 1;
        
    private static final long serialVersionUID = -8572014932719192064L;

    private final Map<String, ModuleSchema> modules;
    
    private final SchemaContext schemaContext;

    public ThriftSchema(String _name, Collection<ModuleSchema.Builder> _modules) throws SchemaBuilderException {
        super(ThriftSchema.class, ThriftSchema.class, null, _name, null);
        this.modules = toMap(this, _modules);
        this.schemaContext = new SchemaContext();
    }
    
    @ThriftField(THRIFT_INDEX_NAME)
    public String getName() {
        return super.getName();
    }

    @ThriftField(THRIFT_INDEX_MODULES)
    public Map<String, ModuleSchema> getModules() {
        return this.modules;
    }
    
    @Override
    SchemaContext getSchemaContext() {
        return this.schemaContext;
    }

    public static final class Builder extends AbstractSchemaBuilder<ThriftSchema, ThriftSchema, ThriftSchema.Builder, ThriftSchema.Builder> {

        public Builder() {
            super(null, ThriftSchema.Builder.class);
        }
        
        private List<ModuleSchema.Builder> modules = New.linkedList();
        
        public ModuleSchema.Builder addModule(String _name) {
            ModuleSchema.Builder result = new ModuleSchema.Builder(this);
            this.modules.add(result);
            return result.name(_name);
        }

        @Override
        protected ThriftSchema _build(ThriftSchema parent) throws SchemaBuilderException {
            super._validate();
            return new ThriftSchema(getName(), this.modules);
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "modules" };
        }
        
        @Override
        @ThriftConstructor
        public ThriftSchema build() throws SchemaBuilderException {
            return this._build(null);
        }
        
    }

}
