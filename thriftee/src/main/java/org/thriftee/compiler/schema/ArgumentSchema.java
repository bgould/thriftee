package org.thriftee.compiler.schema;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.Strings;

public class ArgumentSchema extends BaseSchema<MethodSchema> {

    private final Long identifier;

    private final ISchemaType type;

    private final Boolean required;

    //private final ConstantValue defaultValue;

    protected ArgumentSchema(
            MethodSchema _parent, 
            String _name, 
            ISchemaType _type, 
            Boolean _required, 
            Long _identifier) {
        super(MethodSchema.class, _parent, _name);
        this.type = _type;
        this.required = _required;
        this.identifier = _identifier;
    }

    public Long getIdentifier() {
        return identifier;
    }

    public ISchemaType getType() {
        return type;
    }

    public Boolean getRequired() {
        return required;
    }

    private static final long serialVersionUID = 4332069454537397041L;
    
    public static enum Required {
        REQUIRED, OPTIONAL, NONE;
    }

    public static class Builder extends AbstractSchemaBuilder<MethodSchema, ArgumentSchema, MethodSchema.Builder> {

        private Boolean required;
        
        private ISchemaType type;
        
        private Long identifier;

        public Builder type(ISchemaType type) {
            this.type = type;
            return this;
        }

        public Builder required(Boolean required) {
            this.required = required;
            return this;
        }
        
        public Builder identifier(Long _identifier) {
            this.identifier = _identifier;
            return this;
        }
        
        Builder(MethodSchema.Builder parentBuilder) {
            super(parentBuilder);
        }

        private String name;

        public Builder name(String _name) {
            this.name = _name;
            return this;
        }

        @Override
        protected ArgumentSchema _build(MethodSchema _parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "argument");
            }
            if (type == null) {
                throw new SchemaBuilderException(Messages.SCHEMA_002, "argument");
            }
            ArgumentSchema result = new ArgumentSchema(_parent, this.name, this.type, this.required, this.identifier);
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name" };
        }

    }
    
}
