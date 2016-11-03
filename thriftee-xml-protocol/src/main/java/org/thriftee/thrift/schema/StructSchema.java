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

public final class StructSchema extends AbstractStructSchema<
    ModuleSchema,
    StructSchema,
    StructFieldSchema,
    StructFieldSchema.Builder> {

  private static final long serialVersionUID = 9173725847653740446L;

  private StructSchema(
      final ModuleSchema parent,
      final String name,
      final Collection<StructFieldSchema.Builder> fields,
      final String doc,
      final Collection<ThriftAnnotation> annotations
    ) throws SchemaBuilderException {
    super(
      ModuleSchema.class,
      StructSchema.class,
      parent,
      name,
      fields,
      doc,
      annotations
    );
  }

  @Override
  public String getModuleName() {
    return getParent().getName();
  }

  @Override
  public String getTypeName() {
    return getName();
  }

  public static final class Builder extends 
    AbstractStructSchema.AbstractStructSchemaBuilder<
        ModuleSchema, 
        StructSchema, 
        ModuleSchema.Builder, 
        StructFieldSchema.Builder, 
        StructSchema.Builder
    > {

    protected Builder(ModuleSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
    }

    @Override
    protected StructFieldSchema.Builder _createFieldBuilder() {
      return new StructFieldSchema.Builder(this);
    }

    @Override
    protected StructSchema _createStruct(ModuleSchema _parent)
        throws SchemaBuilderException {
      return new StructSchema(
        _parent,
        getName(),
        _getFields(),
        getDoc(),
        getAnnotations()
      );
    }

  }

}
