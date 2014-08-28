package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.codec.ThriftUnion;

@ThriftUnion
public final class UnionSchema extends BaseSchemaType<ModuleSchema> {

    public static final int THRIFT_INDEX_NAME = 1;
    
    public static final int THRIFT_INDEX_FIELDS = THRIFT_INDEX_NAME + 1;
    
    public static final int THRIFT_INDEX_ANNOTATIONS = THRIFT_INDEX_FIELDS + 1;
    
    private static final long serialVersionUID = 9173725847653740446L;
    
    private final Map<String, UnionFieldSchema> fields;
    
    private UnionSchema(
            ModuleSchema parent, 
            String _name, 
            Collection<UnionFieldSchema.Builder> _fields, 
            Collection<ThriftAnnotation> _annotations
        ) throws SchemaBuilderException {
        super(
            ModuleSchema.class, 
            parent, 
            new ReferenceSchemaType(ThriftProtocolType.STRUCT, parent.getName(), _name), 
            _annotations
        );
        this.fields = toMap(this, _fields);
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

    @ThriftField(THRIFT_INDEX_FIELDS)
    public Map<String, UnionFieldSchema> getFields() {
        return this.fields;
    }
    
    @Override
    public ThriftProtocolType getProtocolType() {
        return ThriftProtocolType.STRUCT;
    }
    
    public ModuleSchema getModule() {
        return getParent();
    }
    
    @Override
    public String getModuleName() {
        return getModule().getName();
    }

    @Override
    public String getTypeName() {
        return getName();
    }

    public static final class Builder extends AbstractSchemaBuilder<ModuleSchema, UnionSchema, ModuleSchema.Builder, UnionSchema.Builder> {

        Builder(ModuleSchema.Builder parentBuilder) {
            super(parentBuilder, Builder.class);
        }
        
        private final List<UnionFieldSchema.Builder> fields = New.linkedList();
        
        public UnionFieldSchema.Builder addField(String _name) {
            UnionFieldSchema.Builder result = new UnionFieldSchema.Builder(this);
            this.fields.add(result);
            return result.name(_name);
        }

        @Override
        protected UnionSchema _build(ModuleSchema _parent) throws SchemaBuilderException {
            super._validate();
            UnionSchema result = new UnionSchema(_parent, getName(), this.fields, getAnnotations());
            return result;
        }

        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "annotations", "fields" };
        }
        
    }
    
}
