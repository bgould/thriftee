package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;
import org.thriftee.util.New;
import org.thriftee.util.Strings;

public class MethodSchema extends BaseSchema<ServiceSchema> {

    private static final long serialVersionUID = -6863308018762813185L;

    private final boolean oneway;

    private final ISchemaType returnType;

    private final ISchemaValue defaultValue;

    private final Map<String, ArgumentSchema> arguments;

    private final Map<String, ArgumentSchema> exceptions;

    protected MethodSchema(
            ServiceSchema _parent, 
            String name, 
            boolean _oneway,
            ISchemaType _returnType, 
            ISchemaValue _defaultValue,
            Collection<ArgumentSchema.Builder> _arguments,
            Collection<ArgumentSchema.Builder> _exceptions) throws SchemaBuilderException {
        super(ServiceSchema.class, _parent, name);
        this.oneway = _oneway;
        this.returnType = _returnType;
        this.defaultValue = _defaultValue;
        this.arguments = toMap(this, _arguments);
        this.exceptions = toMap(this, _exceptions);
    }

    public boolean isOneway() {
        return oneway;
    }

    public ISchemaType getReturnType() {
        return returnType;
    }

    public ISchemaValue defaultValue() {
        return defaultValue;
    }

    public Map<String, ArgumentSchema> getArguments() {
        return arguments;
    }

    public Map<String, ArgumentSchema> getExceptions() {
        return exceptions;
    }

    public static class Builder extends AbstractSchemaBuilder<ServiceSchema, MethodSchema, ServiceSchema.Builder> {

        Builder(ServiceSchema.Builder parentBuilder) {
            super(parentBuilder);
        }
        
        private ISchemaType returnType = PrimitiveTypeSchema.VOID;

        private boolean oneway;
        
        private ISchemaValue defaultValue = null;
        
        private List<ArgumentSchema.Builder> arguments = New.linkedList();
        
        private List<ArgumentSchema.Builder> exceptions = New.linkedList();
        
        public Builder oneway(boolean _oneway) {
            this.oneway = _oneway;
            return this;
        }
        
        public Builder returnType(ISchemaType type) {
            this.returnType = type;
            return this;
        }
        
        public Builder defaultValue(ISchemaValue value) {
            this.defaultValue = value;
            return this;
        }
        
        public ArgumentSchema.Builder addArgument(String _name) {
            ArgumentSchema.Builder result = new ArgumentSchema.Builder(this).name(_name);
            this.arguments.add(result);
            return result;
        }
        
        public ArgumentSchema.Builder addException(String _name) {
            ArgumentSchema.Builder result = new ArgumentSchema.Builder(this).name(_name);
            this.exceptions.add(result);
            return result;
        }

        private String name;

        public Builder name(String _name) {
            this.name = _name;
            return this;
        }

        @Override
        protected MethodSchema _build(ServiceSchema _parent) throws SchemaBuilderException {
            if (Strings.isBlank(name)) {
                throw new SchemaBuilderException(Messages.SCHEMA_001, "enum");
            }
            MethodSchema result = new MethodSchema(
                _parent, 
                this.name, 
                this.oneway, 
                returnType, 
                defaultValue,
                arguments, 
                exceptions
            );
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "oneway", "returnType", "arguments", "exceptions" };
        }

    }

}
