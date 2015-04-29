package org.thriftee.compiler.schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.thriftee.compiler.schema.AbstractFieldSchema.AbstractFieldBuilder;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftProtocolType;

/**
 * 
 * @author bcg
 *
 * @param <P> The parent type for this struct
 * @param <T> The type of this struct
 * @param <F> The type for the fields of this schema
 * @param <FB> The type for the builder for the fields
 */
public abstract class AbstractStructSchema<P extends BaseSchema<?, ?>, T extends BaseSchema<P, T>, F extends AbstractFieldSchema<T, F>, FB extends AbstractFieldBuilder<T, F, ?, ?>> extends BaseSchemaType<P, T> {

    private static final long serialVersionUID = -7411640934657605126L;

    public static final int THRIFT_INDEX_NAME = 1;
    
    public static final int THRIFT_INDEX_FIELDS = THRIFT_INDEX_NAME + 1;
    
    public static final int THRIFT_INDEX_ANNOTATIONS = THRIFT_INDEX_FIELDS + 1;
    
    private final Map<String, F> fields;
    
    protected AbstractStructSchema(
            Class<P> _parentClass,
            Class<T> _thisClass,
            P _parent, 
            String _name, 
            Collection<FB> _fields, 
            Collection<ThriftAnnotation> _annotations
        ) throws SchemaBuilderException {
        super(
            _parentClass, 
            _thisClass,
            _parent, 
            new ReferenceSchemaType(ThriftProtocolType.STRUCT, _parent.getName(), _name), 
            _annotations
        );
        this.fields = toMap($this, _fields);
    }

    @Override
    public String getModuleName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTypeName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThriftProtocolType getProtocolType() {
        // TODO Auto-generated method stub
        return null;
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
    public Map<String, F> getFields() {
        return this.fields;
    }

    public static abstract class AbstractStructSchemaBuilder<
            P extends BaseSchema<?, P>, 
            T extends BaseSchema<P, T>, 
            PB extends AbstractSchemaBuilder<?, P, ?, ?>,
            FB extends AbstractFieldBuilder<?, ?, ?, FB>,
            B extends AbstractStructSchemaBuilder<P, T, PB, FB, B> 
        > extends AbstractSchemaBuilder<P, T, PB, B> {
        
        private final List<FB> fields = New.linkedList();
        
        protected AbstractStructSchemaBuilder(PB parentBuilder, Class<B> thisClass) {
            super(parentBuilder, thisClass);
        }
        
        public FB addField(String _name) {
            FB result = _createFieldBuilder();
            this.fields.add(result);
            return result.name(_name);
        }
        
        protected List<FB> _getFields() {
            return this.fields;
        }
        
        @Override
        protected T _build(P _parent) throws SchemaBuilderException {
            super._validate();
            T result = _createStruct(_parent);
            return result;
        }
        
        protected abstract FB _createFieldBuilder();

        protected abstract T _createStruct(P _parent) throws SchemaBuilderException;
        
        @Override
        protected String[] toStringFields() {
            return new String[] { "name", "annotations", "fields" };
        }

    }

}
