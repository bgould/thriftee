package org.thriftee.compiler.schema;

import java.util.Collection;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;

public abstract class AbstractFieldSchema<P extends BaseSchema<?>> extends BaseSchema<P> {

    private final Long identifier;

    private final ISchemaType type;

    private final Boolean required;

    //private final ConstantValue defaultValue;

    protected AbstractFieldSchema(
            Class<P> parentClass,
            P _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            ISchemaType _type, 
            Boolean _required, 
            Long _identifier) throws SchemaBuilderException {
        super(parentClass, _parent, _name, _annotations);
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

    public static abstract class AbstractFieldBuilder<P extends BaseSchema<?>, T extends BaseSchema<P>, PB extends AbstractSchemaBuilder<?, P, ?, ?>, B extends AbstractFieldBuilder<P, T, PB, B>> 
            extends AbstractSchemaBuilder<P, T, PB, B> {

        private Boolean required;
        
        private ISchemaType type;
        
        private Long identifier;
        
        protected AbstractFieldBuilder(PB parentBuilder, Class<B> thisClass) {
            super(parentBuilder, thisClass);
        }

        public final B type(ISchemaType type) {
            this.type = type;
            return $this;
        }

        public final B required(Boolean required) {
            this.required = required;
            return $this;
        }
        
        public final B identifier(Long _identifier) {
            this.identifier = _identifier;
            return $this;
        }
        
        protected final ISchemaType getType() {
            return this.type;
        }
        
        protected final Boolean isRequired() {
            return this.required;
        }
        
        protected final Long getIdentifier() {
            return this.identifier;
        }
        
        @Override
        protected T _build(P _parent) throws SchemaBuilderException {
            super._validate();
            if (type == null) {
                throw new SchemaBuilderException(Messages.SCHEMA_002, _fieldTypeName());
            }
            T result = _buildInstance(_parent);
            return result;
        }
        
        protected abstract String _fieldTypeName();
        
        protected abstract T _buildInstance(P _parent) throws SchemaBuilderException;

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "annotations", "type", "required", "identifier" };
        }

    }
    
}
