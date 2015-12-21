/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.compiler.schema;

import static org.thriftee.compiler.schema.SchemaBuilderException.Messages.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.util.Strings;

/**
 * @param <S> type of parent schema object for the result of this builder
 * @param <T> type of schema object this builder produces
 * @param <P> parent of parent builder to return when end() is called
 * @param <B> the canonical builder type
 */
public abstract class AbstractSchemaBuilder<
    P extends BaseSchema<?, P>, 
    T extends BaseSchema<P, T>, 
    PB extends AbstractSchemaBuilder<?, P, ?, ?>, 
    B extends AbstractSchemaBuilder<P, T, PB, B>
  > {
  
  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  
  private final PB parentBuilder;
  
  private String name;
  
  private final List<ThriftAnnotation> annotations = new ArrayList<>();

  protected abstract T _build(P parent) throws SchemaBuilderException;
  
  protected abstract String[] toStringFields();
  
  protected final Class<B> $thisClass;
  
  protected final B $this;
  
  AbstractSchemaBuilder(final PB _parentBuilder, Class<B> thisClass) {
    super();
    this.parentBuilder = _parentBuilder;
    this.$thisClass = thisClass;
    this.$this = thisClass.cast(this);
  }

  protected PB getParentBuilder() {
    return this.parentBuilder;
  }
  
  public final B name(final String _name) {
    this.name = _name;
    return $this;
  }
  
  public final B addAnnotation(final String _name, final String _value) {
    final ThriftAnnotation _annotation = new ThriftAnnotation(_name, _value);
    this.annotations.add(_annotation);
    return $this;
  }
  
  public PB end() {
    return getParentBuilder();
  }
  
  public abstract T build() throws SchemaBuilderException;
  
  protected String getName() {
    return this.name;
  }
  
  protected Collection<ThriftAnnotation> getAnnotations() {
    return Collections.unmodifiableCollection(this.annotations);
  }
  
  protected void _validate() throws SchemaBuilderException {
    if (Strings.isBlank(name)) {
      throw new SchemaBuilderException(SCHEMA_001, $thisClass.getSimpleName());
    }
    final HashSet<String> annotationNames = new HashSet<String>();
    for (ThriftAnnotation annotation : getAnnotations()) {
      if (annotationNames.contains(annotation.getName())) {
        throw new SchemaBuilderException(SCHEMA_003, "annotations", annotation.getName());
      }
    }
  }
  
  private static final ConcurrentHashMap<Class<?>, ToStringFields<?>> fieldMap = 
      new ConcurrentHashMap<Class<?>, ToStringFields<?>>();
  
  @Override
  public final String toString() {
    return fieldsFor(this).toString(this);
  }
  
  @SuppressWarnings("unchecked")
  private static <T extends AbstractSchemaBuilder<?, ?, ?, ?>> ToStringFields<T> fieldsFor(T obj) {
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
