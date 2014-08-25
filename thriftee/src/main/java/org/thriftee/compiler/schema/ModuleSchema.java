package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.Map;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.New;
import org.thriftee.util.Strings;

public class ModuleSchema extends BaseSchema<ThriftSchema> {

    private static final long serialVersionUID = 1973580748761800425L;

    private final Map<String, ServiceSchema> services;
    
    private final Map<String, StructSchema> structs;
    
    private final Map<String, EnumSchema> enums;
    
    public ModuleSchema(
            ThriftSchema _parent,
            String _name, 
            Collection<ServiceSchema.Builder> _services, 
            Collection<StructSchema.Builder> _structs, 
            Collection<EnumSchema.Builder> _enums) throws SchemaBuilderException {
        super(ThriftSchema.class, _parent, _name);
        this.services = toMap(this, _services);
        this.structs = toMap(this, _structs);
        this.enums = toMap(this, _enums);
    }
    
    public Map<String, ServiceSchema> getServices() {
        return services;
    }

    public Map<String, StructSchema> getStructs() {
        return structs;
    }

    public Map<String, EnumSchema> getEnums() {
        return enums;
    }
    
    public static class Builder extends AbstractSchemaBuilder<ThriftSchema, ModuleSchema, ThriftSchema.Builder> {

        private final Map<String, ServiceSchema.Builder> services = New.map();
        
        private final Map<String, StructSchema.Builder> structs = New.map();
        
        private final Map<String, EnumSchema.Builder> enums = New.map();
        
        Builder(ThriftSchema.Builder parentBuilder) {
            super(parentBuilder);
        }

        private String name;
        
        public Builder name(String _name) {
            this.name = _name;
            return this;
        }
        
        @Override
        protected ModuleSchema _build(ThriftSchema parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "enum value");
            }
            final ModuleSchema result = new ModuleSchema(
                parent, 
                name, 
                services.values(),
                structs.values(),
                enums.values()
            );
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "services", "structs", "enums" };
        }
        
    }
    
}
