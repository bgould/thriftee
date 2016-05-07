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

import java.util.Collection;

import org.thriftee.compiler.schema.MethodArgumentSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class MethodArgumentSchema extends AbstractStructSchema<MethodSchema, MethodArgumentSchema, MethodArgumentFieldSchema, MethodArgumentFieldSchema.Builder> {

  private static final long serialVersionUID = 9173725847653740446L;

  private MethodArgumentSchema(
      MethodSchema parent, 
      String _name, 
      Collection<MethodArgumentFieldSchema.Builder> _fields, 
      Collection<ThriftAnnotation> _annotations
    ) throws SchemaBuilderException {
    super(
      MethodSchema.class, 
      MethodArgumentSchema.class,
      parent, 
      _name,
      _fields,
      _annotations
    );
  }


  @Override
  public String getModuleName() {
    return getParent().getParent().getParent().getName();
  }

  @Override
  public String getTypeName() {
    return getParent().getParent().getName() + "." + getName();
  }

  public static final class Builder extends AbstractStructSchema.AbstractStructSchemaBuilder<
    MethodSchema, 
    MethodArgumentSchema, 
    MethodSchema.Builder, 
    MethodArgumentFieldSchema.Builder, 
    MethodArgumentSchema.Builder> {

    public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
      this(null);
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

    protected Builder(MethodSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
    }

    @Override
    protected MethodArgumentFieldSchema.Builder _createFieldBuilder() {
      return new MethodArgumentFieldSchema.Builder(this);
    }

    @Override
    protected MethodArgumentSchema _createStruct(MethodSchema _parent) throws SchemaBuilderException {
      return new MethodArgumentSchema(_parent, getName(), _getFields(), getAnnotations());
    }

    public MethodArgumentFieldSchema.Builder addArgument(String _name) {
      return addField(_name);
    }

    @Override
    @ThriftConstructor
    public MethodArgumentSchema build() throws SchemaBuilderException {
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

  }

}
