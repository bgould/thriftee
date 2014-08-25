package org.thriftee.compiler.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <S> type of parent schema object for the result of this builder
 * @param <T> type of schema object this builder produces
 * @param <P> parent of parent builder to return when end() is called
 */
abstract class AbstractSchemaBuilder<P extends BaseSchema<?>, T extends BaseSchema<P>, PB extends AbstractSchemaBuilder<?, P, ?>> {
    
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    
    private final PB parentBuilder;

    protected abstract T _build(P parent) throws SchemaBuilderException;
    
    protected abstract String[] toStringFields();
    
    AbstractSchemaBuilder(PB parentBuilder) {
        super();
        this.parentBuilder = parentBuilder;
    }

    protected PB getParentBuilder() {
        return this.parentBuilder;
    }
    
    public PB end() {
        return getParentBuilder();
    }
    
    private static final ConcurrentHashMap<Class<?>, ToStringFields<?>> fieldMap = 
            new ConcurrentHashMap<Class<?>, ToStringFields<?>>();
    
    @Override
    public final String toString() {
        return fieldsFor(this).toString(this);
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends AbstractSchemaBuilder<?, ?, ?>> ToStringFields<T> fieldsFor(T obj) {
        final Class<T> type = (Class<T>) obj.getClass();
        if (!fieldMap.containsKey(type)) {
            ToStringFields<T> fields = new ToStringFields<T>(type, obj.toStringFields());
            fieldMap.put(fields.type, fields);
        }
        return (ToStringFields<T>) fieldMap.get(type);
    }
    
    private static final class ToStringFields<T> {

        private final Field[] fields;

        private final Class<T> type;

        private ToStringFields(final Class<T> _type, final String[] _fields) {
            this.type = _type;
            this.fields = reflectionFieldArray(_type, _fields);
        }
        
        public String toString(T obj) {
            StringBuilder strb = new StringBuilder();
            strb.append(getClass().getSimpleName()).append("({");
            for (int i = 0, c = fields.length; i < c; i++) {
                Field field = fields[i];
                strb.append(field.getName()).append("=");
                try {
                    Object value = field.get(obj);
                    if (value == null) {
                        strb.append("null");
                    }
                    else if (Collection.class.isAssignableFrom(field.getType())) {
                        strb.append(field.getType().getSimpleName()).append("(size: ").append(
                            ((Collection<?>) value).size()
                        ).append(")");
                    } else {
                        strb.append(value.toString());
                    }
                } catch (IllegalAccessException e) {
                    strb.append(field.getName()).append("=").append("ACCESS_ERR");
                }
                if (i + 1 < c) {
                    strb.append(", ");
                }
            }
            strb.append("})");
            return strb.toString();
        }
        
        public static Field[] reflectionFieldArray(final Class<?> _type, final String[] _fields) {
            final List<Field> fieldList = new ArrayList<Field>(_fields.length);
            for (final String fieldname : _fields) {
                Field field = null;
                for (Class<?> type = _type; type != null; type = type.getSuperclass() ) {
                    try {
                        field = type.getDeclaredField(fieldname);
                        if (field != null) {
                            break;
                        }
                    } catch (NoSuchFieldException e) {}
                }
                if (field == null) {
                    throw new IllegalArgumentException("invalid field name `" + fieldname + "` for " + _type);
                } else {
                    fieldList.add(field);
                }
            }
            return fieldList.toArray(new Field[fieldList.size()]);
        }

    }

}
