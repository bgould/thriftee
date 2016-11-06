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

public final class MethodArgsSchema extends AbstractStructSchema<
    MethodSchema,
    MethodArgsSchema,
    MethodArgSchema,
    MethodArgSchema.Builder
  > {

  private static final long serialVersionUID = 9173725847653740446L;

  private MethodArgsSchema(
      final MethodSchema parent,
      final String name,
      final Collection<MethodArgSchema.Builder> fields,
      final String doc,
      final Collection<ThriftAnnotation> annotations
    ) throws SchemaBuilderException {
    super(
      MethodSchema.class,
      MethodArgsSchema.class,
      parent,
      name,
      fields,
      doc,
      annotations
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

  public static final class Builder
      extends AbstractStructSchema.AbstractStructSchemaBuilder<
        MethodSchema, 
        MethodArgsSchema, 
        MethodSchema.Builder, 
        MethodArgSchema.Builder, 
        MethodArgsSchema.Builder
      > {

    protected Builder(MethodSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
    }

    @Override
    protected MethodArgSchema.Builder _createFieldBuilder() {
      return new MethodArgSchema.Builder(this);
    }

    @Override
    protected MethodArgsSchema _createStruct(MethodSchema _parent) 
        throws SchemaBuilderException {
      return new MethodArgsSchema(
        _parent, 
        getName(), 
        _getFields(),
        getDoc(),
        getAnnotations()
      );
    }

    public MethodArgSchema.Builder addArgument(String _name) {
      return addField(_name);
    }

  }

}
