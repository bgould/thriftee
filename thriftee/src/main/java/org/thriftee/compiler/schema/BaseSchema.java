package org.thriftee.compiler.schema;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseSchema<P extends BaseSchema<?>> implements ISchema, Serializable {

    private static final long serialVersionUID = 2582644689032708659L;

    private final SchemaContext schemaContext;

    private final String name;
    
    private final Class<P> parentType;
    
    private final P parent;

    protected BaseSchema(Class<P> parentClass, P parent, String _name) {
        this.parentType = parentClass;
        this.parent = parent;
        if (this.parentType.equals(getClass())) {
            this.schemaContext = null;
        } else {
            this.schemaContext = parent.getSchemaContext();
        }
        this.name = _name;
    }
    
    protected Class<P> getParentType() {
        return this.parentType;
    }
    
    protected P getParent() {
        return this.parent;
    }

    SchemaContext getSchemaContext() {
        if (getParent() != null) {
            return this.schemaContext;
        } else {
            throw new IllegalStateException(
                "parent cannot be null without reimplementing getSchemaContext()");
        }
    }

    public String getName() {
        return this.name;
    }

    protected static <P extends BaseSchema<?>, T extends BaseSchema<P>> Map<String, T> 
            toMap(P parent, Collection<? extends AbstractSchemaBuilder<P, T, ?>> collection) 
                throws SchemaBuilderException {
        final Map<String, T> result = new LinkedHashMap<String, T>();
        for (AbstractSchemaBuilder<P, T, ?> builder : collection) {
            T obj = builder._build(parent);
            result.put(obj.getName(), obj);
        }
        return Collections.unmodifiableMap(result);
    }

}
