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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

abstract class BaseSchema<P extends BaseSchema<?, ?>, 
                          T extends BaseSchema<P, T>> implements Serializable {

  private static final long serialVersionUID = 2582644689032708659L;

  protected final Class<P> parentType;

  protected final Class<T> thisType;

  private final String name;

  private final P parent;

  protected final T $this;

  private final Map<String, ThriftAnnotation> annotations;

  protected BaseSchema(
      Class<P> parentClass,
      Class<T> thisClass,
      P parent, 
      String _name, 
      Collection<ThriftAnnotation> _annotations) throws SchemaBuilderException {
    this.parentType = parentClass;
    this.thisType = thisClass;
    this.parent = parent;
    this.$this = thisClass.cast(this);
    this.name = _name;
    final Map<String, ThriftAnnotation> annotationMap = new LinkedHashMap<>();
    if (_annotations != null) {
      for (ThriftAnnotation annotation : _annotations) {
        if (annotationMap.containsKey(annotation.getName())) {
          throw new SchemaBuilderException(
            SchemaBuilderException.Messages.SCHEMA_003, "annotations", name);
        }
      }
    }
    this.annotations = Collections.unmodifiableMap(annotationMap);
  }
  
  protected Class<P> getParentType() {
    return this.parentType;
  }
  
  protected P getParent() {
    return this.parent;
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
    return this.name;
  }
  
  public Map<String, ThriftAnnotation> getAnnotations() {
    return this.annotations;
  }

  protected static <P extends BaseSchema<?, P>, 
                    T extends BaseSchema<P, T>> Map<String, T> 
      toMap(P parent, Collection<? extends AbstractSchemaBuilder<P, T, ?, ?>> collection) 
        throws SchemaBuilderException {
    final Map<String, T> result = new LinkedHashMap<String, T>();
    for (AbstractSchemaBuilder<P, T, ?, ?> builder : collection) {
      T obj = builder._build(parent);
      String name = obj.getName();
      if (result.containsKey(name)) {
        throw new SchemaBuilderException(SCHEMA_003, "map", name);
      }
      result.put(obj.getName(), obj);
    }
    return Collections.unmodifiableMap(result);
  }

}