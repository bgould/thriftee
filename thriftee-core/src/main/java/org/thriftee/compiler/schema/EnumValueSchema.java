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

import org.thriftee.compiler.schema.EnumValueSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class EnumValueSchema extends BaseSchema<EnumSchema, EnumValueSchema> {
  
  public static final int THRIFT_INDEX_NAME = 1;
  
  public static final int THRIFT_INDEX_VALUE = THRIFT_INDEX_NAME + 1;
  
  public static final int THRIFT_INDEX_VALUE_EXPLICIT = THRIFT_INDEX_VALUE + 1;
  
  private final Long explicitValue;
  
  private final long effectiveValue;

  private EnumValueSchema(EnumSchema _parent, String _name, Long _explicitValue) throws SchemaBuilderException {
    super(EnumSchema.class, EnumValueSchema.class, _parent, _name, null);
    this.explicitValue = _explicitValue;
    // TODO: need to set effectiveValue for enum value schema
    this.effectiveValue = 0;
  }

  @ThriftField(value=THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }

  @ThriftField(value=THRIFT_INDEX_VALUE)
  public long getValue() {
    return this.effectiveValue;
  }
  
  @ThriftField(value=THRIFT_INDEX_VALUE_EXPLICIT)
  public boolean isValueExplicit() {
    return explicitValue != null;
  }

  public EnumSchema getEnum() {
    return getParent();
  }
  
  private static final long serialVersionUID = 2125692491877946279L;
  
  public static final class Builder extends AbstractSchemaBuilder<EnumSchema, EnumValueSchema, EnumSchema.Builder, EnumValueSchema.Builder> {
    
    private Long explicitValue = null;
    
    public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
      this(null);
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }
    
    Builder(EnumSchema.Builder parent) {
      super(parent, Builder.class);
    }
    
    public Builder explicitValue(Long explicitValue) {
      this.explicitValue = explicitValue;
      return this;
    }
    
    @Override
    protected EnumValueSchema _build(EnumSchema _parent) throws SchemaBuilderException {
      super._validate();
      EnumValueSchema result = new EnumValueSchema(_parent, getName(), explicitValue);
      return result;
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "explicitValue" };
    }
    
    @Override
    @ThriftConstructor
    public EnumValueSchema build() throws SchemaBuilderException {
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }
    
  }

}
