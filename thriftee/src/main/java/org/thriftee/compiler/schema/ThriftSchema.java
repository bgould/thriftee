package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.Map;

public class ThriftSchema extends BaseSchema<ThriftSchema> {

    private static final long serialVersionUID = -8572014932719192064L;

    private final Map<String, ModuleSchema> modules;

    public ThriftSchema(String _name, Collection<ModuleSchema.Builder> _modules) throws SchemaBuilderException {
        super(ThriftSchema.class, null, _name);
        this.modules = toMap(this, _modules);
    }

    public Map<String, ModuleSchema> getModules() {
        return this.modules;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static class Builder extends AbstractSchemaBuilder<ThriftSchema, ThriftSchema, ThriftSchema.Builder> {

        Builder(Builder parentBuilder) {
            super(parentBuilder);
        }

        @Override
        protected ThriftSchema _build(ThriftSchema parent) throws SchemaBuilderException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected String[] toStringFields() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }

}
