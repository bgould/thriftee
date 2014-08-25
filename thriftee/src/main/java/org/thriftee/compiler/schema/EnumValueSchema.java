package org.thriftee.compiler.schema;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.Strings;

public class EnumValueSchema extends BaseSchema<EnumSchema> {

    private final EnumSchema parent;
    
    private final int intConstant;

    private EnumValueSchema(EnumSchema _parent, String _name, int _intConstant) {
        super(EnumSchema.class, _parent, _name);
        this.parent = _parent;
        this.intConstant = _intConstant;
    }

    public EnumSchema getEnum() {
        return this.parent;
    }

    public int getIntConstant() {
        return this.intConstant;
    }

    private static final long serialVersionUID = 2125692491877946279L;
    
    public static class Builder extends AbstractSchemaBuilder<EnumSchema, EnumValueSchema, EnumSchema.Builder> {
        
        private String name;
        
        private int intConstant = -1;
        
        Builder(EnumSchema.Builder parent) {
            super(parent);
        }
        
        public Builder name(String _name) {
            this.name = _name;
            return this;
        }
        
        @Override
        protected EnumValueSchema _build(EnumSchema _parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "enum value");
            }
            if (intConstant < 0) {
                throw new SchemaBuilderException(Messages.SCHEMA_901);
            }
            EnumValueSchema result = new EnumValueSchema(_parent, this.name, intConstant);
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "intConstant" };
        }
        
    }

}
