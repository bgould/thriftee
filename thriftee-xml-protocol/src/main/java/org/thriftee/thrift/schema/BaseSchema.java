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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class BaseSchema<P extends BaseSchema<?, ?>,
                          T extends BaseSchema<P, T>> implements Serializable {

  private static final long serialVersionUID = 2582644689032708659L;

  protected final Class<P> _parentType;

  protected final Class<T> _thisType;

  private final String _name;

  private final P _parent;

  protected final T _this;

  private final String _doc;

  private final Map<String, ThriftAnnotation> _annotations;

  protected BaseSchema(
      Class<P> parentClass,
      Class<T> thisClass,
      P parent,
      String _name,
      String doc,
      Collection<ThriftAnnotation> annotations) throws SchemaBuilderException {
    this._parentType = parentClass;
    this._thisType = thisClass;
    this._parent = parent;
    this._this = thisClass.cast(this);
    this._name = _name;
    this._doc = (doc == null) ? "" : doc;
    final Map<String, ThriftAnnotation> annotationMap = new LinkedHashMap<>();
    if (annotations != null) {
      for (ThriftAnnotation annotation : annotations) {
        if (annotationMap.containsKey(annotation.getName())) {
          throw SchemaBuilderException.duplicateName("annotations", _name);
        }
      }
    }
    this._annotations = Collections.unmodifiableMap(annotationMap);
  }

  protected Class<P> getParentType() {
    return this._parentType;
  }

  protected P getParent() {
    return this._parent;
  }

  public final ThriftSchema getRoot() {
    if (this instanceof ThriftSchema) {
      return (ThriftSchema) this;
    } else {
      return getParent().getRoot();
    }
  }

  SchemaContext getSchemaContext() {
    if (getParent() != null) {
      return getParent().getSchemaContext();
    } else {
      throw new IllegalStateException(
        "parent cannot be null without reimplementing getSchemaContext()");
    }
  }

  String getName() {
    return this._name;
  }

  public String getDoc() {
    return this._doc;
  }

  public Map<String, ThriftAnnotation> getAnnotations() {
    return this._annotations;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[name=" + getName() + "]";
  }

  protected static <P extends BaseSchema<?, P>,
                    T extends BaseSchema<P, T>> Map<String, T>
      toMap(P parent, Collection<? extends AbstractSchemaBuilder<P, T, ?, ?>> collection)
        throws SchemaBuilderException {
    final Map<String, T> result = new LinkedHashMap<>();
    for (AbstractSchemaBuilder<P, T, ?, ?> builder : collection) {
      T obj = builder.build(parent);
      String name = obj.getName();
      if (result.containsKey(name)) {
        throw SchemaBuilderException.duplicateName("map", name);
      }
      result.put(obj.getName(), obj);
    }
    return Collections.unmodifiableMap(result);
  }

}
