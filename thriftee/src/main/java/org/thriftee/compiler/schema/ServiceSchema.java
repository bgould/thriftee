package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.New;
import org.thriftee.util.Strings;

public class ServiceSchema extends BaseSchema<ModuleSchema> {

    private static final long serialVersionUID = 419978455931497309L;

    private final Map<String, MethodSchema> methods;
    
    public ServiceSchema(ModuleSchema module/*, parent service? */, String _name, Collection<MethodSchema.Builder> _methods) 
            throws SchemaBuilderException {
        super(ModuleSchema.class, module, _name);
        this.methods = toMap(this, _methods);
    }
    
    public Map<String, MethodSchema> getMethods() {
        return methods;
    }

    public static class Builder extends AbstractSchemaBuilder<ModuleSchema, ServiceSchema, ModuleSchema.Builder> {

        private List<MethodSchema.Builder> methods = New.linkedList();
        
        Builder(final ModuleSchema.Builder parentBuilder) {
            super(parentBuilder);
        }
        
        public MethodSchema.Builder addMethod(final String _name) {
            MethodSchema.Builder result = new MethodSchema.Builder(this);
            return result.name(_name);
        }

        private String name;

        public Builder name(final String _name) {
            this.name = _name;
            return this;
        }

        @Override
        protected ServiceSchema _build(final ModuleSchema _parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "Service");
            }
            final ServiceSchema result = new ServiceSchema(_parent, this.name, methods);
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name" };
        }
        
    }
}
