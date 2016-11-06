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

public final class EnumValueSchema
    extends BaseSchema<EnumSchema, EnumValueSchema> {

  private final int _value;

  private EnumValueSchema(
        final EnumSchema parent, 
        final String name, 
        final int explicitValue,
        final String doc,
        final Collection<ThriftAnnotation> annotations
      ) throws SchemaBuilderException {
    super(
      EnumSchema.class,
      EnumValueSchema.class,
      parent,
      name,
      doc,
      annotations
    );
    this._value = explicitValue;
  }

  public String getName() {
    return super.getName();
  }

  public long getValue() {
    return this._value;
  }

  public EnumSchema getEnum() {
    return getParent();
  }

  private static final long serialVersionUID = 2125692491877946279L;

  public static final class Builder extends AbstractSchemaBuilder<
      EnumSchema,
      EnumValueSchema,
      EnumSchema.Builder,
      EnumValueSchema.Builder> {

    private final int _value;

    Builder(EnumSchema.Builder parent, int value) {
      super(parent, Builder.class);
      this._value = value;
    }

    @Override
    protected EnumValueSchema build(EnumSchema parent)
        throws SchemaBuilderException {
      super._validate();
      EnumValueSchema result = new EnumValueSchema(
        parent, getName(), _value, getDoc(), getAnnotations()
      );
      return result;
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "value" };
    }

  }

}
