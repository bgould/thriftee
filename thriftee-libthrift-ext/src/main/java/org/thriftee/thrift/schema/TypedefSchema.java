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

public final class TypedefSchema
    extends BaseSchemaType<ModuleSchema, TypedefSchema>
    implements SchemaType {

  private static final long serialVersionUID = 1989609882619531243L;

  private final SchemaType _type;

  private TypedefSchema(
        final ModuleSchema parent,
        final String name,
        final SchemaType type,
        final String doc,
        final Collection<ThriftAnnotation> annotations
      ) throws SchemaBuilderException {
    super(
      ModuleSchema.class,
      TypedefSchema.class,
      parent,
      new SchemaReference(SchemaReference.Type.TYPEDEF, parent.getName(), name),
      doc,
      annotations
    );
    this._type = SchemaBuilderException.ensureNotNull("type", type);
  }

  @Override
  public String getModuleName() {
    return getParent().getName();
  }

  @Override
  public String getTypeName() {
    return getName();
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return _type.getProtocolType();
  }

  public static final class Builder extends AbstractSchemaBuilder<
      ModuleSchema,
      TypedefSchema,
      ModuleSchema.Builder,
      Builder> {

    Builder(ModuleSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
    }

    private SchemaType _type;

    public Builder type(SchemaType type) {
      this._type = type;
      return this;
    }

    @Override
    protected TypedefSchema build(
        final ModuleSchema _parent) throws SchemaBuilderException {
      super._validate();
      return new TypedefSchema(
        _parent,
        getName(),
        _type,
        getDoc(),
        getAnnotations()
      );
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "enumValues" };
    }

  }

}
