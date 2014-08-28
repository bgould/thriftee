package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class MethodSchema extends BaseSchema<ServiceSchema, MethodSchema> {
    
    public static final int THRIFT_INDEX_NAME = 1;
    
    public static final int THRIFT_INDEX_ONEWAY = THRIFT_INDEX_NAME + 1;
    
    public static final int THRIFT_INDEX_RETURN_TYPE = THRIFT_INDEX_ONEWAY + 1;
    
    public static final int THRIFT_INDEX_ARGUMENTS = THRIFT_INDEX_RETURN_TYPE + 1;
    
    public static final int THRIFT_INDEX_EXCEPTIONS = THRIFT_INDEX_ARGUMENTS + 1;
    
    public static final int THRIFT_INDEX_ANNOTATIONS = THRIFT_INDEX_EXCEPTIONS + 1;
    
    private static final long serialVersionUID = -6863308018762813185L;

    private final boolean oneway;

    private final ThriftSchemaType returnType;

    private final Map<String, MethodArgumentSchema> arguments;

    private final Map<String, MethodThrowsSchema> exceptions;

    protected MethodSchema(
            ServiceSchema _parent, 
            String _name, 
            Collection<ThriftAnnotation> _annotations,
            boolean _oneway,
            ISchemaType _returnType, 
            Collection<MethodArgumentSchema.Builder> _arguments,
            Collection<MethodThrowsSchema.Builder> _exceptions) throws SchemaBuilderException {
        super(ServiceSchema.class, MethodSchema.class, _parent, _name, _annotations);
        this.oneway = _oneway;
        this.returnType = ThriftSchemaType.wrap(_returnType);
        this.arguments = toMap(this, _arguments);
        this.exceptions = toMap(this, _exceptions);
    }
    
    @Override
    @ThriftField(THRIFT_INDEX_NAME)
    public String getName() {
        return super.getName();
    }

    @Override
    @ThriftField(THRIFT_INDEX_ANNOTATIONS)
    public Map<String, ThriftAnnotation> getAnnotations() {
        return super.getAnnotations();
    }

    @ThriftField(THRIFT_INDEX_ONEWAY)
    public boolean isOneway() {
        return oneway;
    }

    @ThriftField(THRIFT_INDEX_RETURN_TYPE)
    public ThriftSchemaType getReturnType() {
        return returnType;
    }

    @ThriftField(THRIFT_INDEX_ARGUMENTS)
    public Map<String, MethodArgumentSchema> getArguments() {
        return arguments;
    }

    @ThriftField(THRIFT_INDEX_EXCEPTIONS)
    public Map<String, MethodThrowsSchema> getExceptions() {
        return exceptions;
    }

    public static class Builder extends AbstractSchemaBuilder<ServiceSchema, MethodSchema, ServiceSchema.Builder, Builder> {

        Builder(ServiceSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }
        
        private ISchemaType returnType = PrimitiveTypeSchema.VOID;

        private boolean oneway;
        
        private List<MethodArgumentSchema.Builder> arguments = New.linkedList();
        
        private List<MethodThrowsSchema.Builder> exceptions = New.linkedList();
        
        public Builder oneway(boolean _oneway) {
            this.oneway = _oneway;
            return this;
        }
        
        public Builder returnType(ISchemaType type) {
            this.returnType = type;
            return this;
        }
        
        public Builder defaultValue(ISchemaValue value) {
            return this;
        }
        
        public MethodArgumentSchema.Builder addArgument(String _name) {
            MethodArgumentSchema.Builder result = new MethodArgumentSchema.Builder(this).name(_name);
            this.arguments.add(result);
            return result;
        }
        
        public MethodThrowsSchema.Builder addThrows(String _name) {
            MethodThrowsSchema.Builder result = new MethodThrowsSchema.Builder(this).name(_name);
            this.exceptions.add(result);
            return result;
        }

        @Override
        protected MethodSchema _build(ServiceSchema _parent) throws SchemaBuilderException {
            super._validate();
            MethodSchema result = new MethodSchema(
                _parent, 
                getName(),
                getAnnotations(),
                this.oneway, 
                returnType, 
                arguments, 
                exceptions
            );
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "oneway", "returnType", "arguments", "exceptions", "annotations" };
        }

    }

}
