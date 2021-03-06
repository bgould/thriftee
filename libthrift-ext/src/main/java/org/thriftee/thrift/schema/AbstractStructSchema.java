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

 import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.thriftee.thrift.schema.AbstractFieldSchema.AbstractFieldBuilder;

/**
 * 
 * @author bcg
 *
 * @param <P> The parent type for this struct
 * @param <T> The type of this struct
 * @param <F> The type for the fields of this schema
 * @param <FB> The type for the builder for the fields
 */
public abstract class AbstractStructSchema<
    P extends BaseSchema<?, ?>, 
    T extends BaseSchema<P, T>, 
    F extends AbstractFieldSchema<T, F>, 
    FB extends AbstractFieldBuilder<T, F, ?, ?>
  > extends BaseSchemaType<P, T> {

  private static final long serialVersionUID = -7411640934657605126L;

  private final Map<String, F> fields;

  private final Map<Short, F> fieldsById;

  protected AbstractStructSchema(
      final Class<P> _parentClass,
      final Class<T> _thisClass,
      final P parent, 
      final String name, 
      final Collection<FB> fields,
      final String doc,
      final Collection<ThriftAnnotation> annotations
    ) throws SchemaBuilderException {
    super(
      _parentClass, 
      _thisClass,
      parent,
      new SchemaReference(SchemaReference.Type.STRUCT, parent.getName(), name),
      doc,
      annotations
    );
    this.fields = toMap(_this, fields);
    final Map<Short, F> fieldsById = new HashMap<>();
    for (final F field : this.fields.values()) {
      fieldsById.put(field.getIdentifier(), field);
    }
    this.fieldsById = Collections.unmodifiableMap(fieldsById);
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return ThriftProtocolType.STRUCT;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Map<String, ThriftAnnotation> getAnnotations() {
    return super.getAnnotations();
  }

  public Map<String, F> getFields() {
    return this.fields;
  }

  public F getField(Short id) {
    return this.fieldsById.get(id);
  }

  public static abstract class AbstractStructSchemaBuilder<
      P extends BaseSchema<?, P>, 
      T extends BaseSchema<P, T>, 
      PB extends AbstractSchemaBuilder<?, P, ?, ?>,
      FB extends AbstractFieldBuilder<?, ?, ?, FB>,
      B extends AbstractStructSchemaBuilder<P, T, PB, FB, B> 
    > extends AbstractSchemaBuilder<P, T, PB, B> {
    
    private final List<FB> fields = new LinkedList<>();
    
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
    protected T build(P _parent) throws SchemaBuilderException {
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
