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
package org.thriftee.thrift.schema;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

  private final PB _parentBuilder;

  private String _name;

  private String _doc;

  private final List<ThriftAnnotation> _annotations = new ArrayList<>();

  protected final Class<B> $thisClass;

  protected final B $this;

  AbstractSchemaBuilder(final PB _parentBuilder, Class<B> thisClass) {
    super();
    this._parentBuilder = _parentBuilder;
    this.$thisClass = thisClass;
    this.$this = thisClass.cast(this);
  }

  public B name(final String name) {
    this._name = name;
    return $this;
  }

  public B doc(final String doc) {
    this._doc = doc;
    return $this;
  }

  public final B addAnnotation(final String name, final String value) {
    final ThriftAnnotation _annotation = new ThriftAnnotation(name, value);
    this._annotations.add(_annotation);
    return $this;
  }

  public PB end() {
    return getParentBuilder();
  }

  protected PB getParentBuilder() {
    return this._parentBuilder;
  }

  protected abstract T build(P parent) throws SchemaBuilderException;

  protected abstract String[] toStringFields();

  protected String getName() {
    return this._name;
  }

  protected String getDoc() {
    return this._doc;
  }

  protected Collection<ThriftAnnotation> getAnnotations() {
    return Collections.unmodifiableCollection(this._annotations);
  }

  protected void _validate() throws SchemaBuilderException {
    if (_name == null || _name.trim().equals("")) {
      throw SchemaBuilderException.nameCannotBeNull($thisClass.getName());
    }
    final HashSet<String> annotationNames = new HashSet<String>();
    for (final ThriftAnnotation annotation : getAnnotations()) {
      final String name = annotation.getName();
      if (annotationNames.contains(name)) {
        throw SchemaBuilderException.duplicateName("annotations", name);
      }
    }
  }

  private static final ConcurrentHashMap<Class<?>, ToStringFields<?>> fieldMap = new ConcurrentHashMap<>();

  @Override
  public final String toString() {
    return fieldsFor(this).toString(this);
  }

  @SuppressWarnings("unchecked")
  private static <T extends AbstractSchemaBuilder<?, ?, ?, ?>> ToStringFields<T> fieldsFor(T obj) {
    final Class<T> type = (Class<T>) obj.getClass();
    if (!fieldMap.containsKey(type)) {
      final ToStringFields<T> fields = new ToStringFields<T>(type, obj.toStringFields());
      fieldMap.putIfAbsent(fields.type, fields);
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
