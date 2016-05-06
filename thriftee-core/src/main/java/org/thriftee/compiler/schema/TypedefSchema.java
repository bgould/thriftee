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

import java.nio.file.attribute.AclEntry.Builder;
import java.util.Collection;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public class TypedefSchema 
    extends BaseSchemaType<ModuleSchema, TypedefSchema> 
    implements ISchemaType {

  private static final long serialVersionUID = 1989609882619531243L;

  public static final int THRIFT_INDEX_NAME = 1;

  private final ThriftSchemaType _type;
  
  public TypedefSchema(
        final ModuleSchema parent,
        final String name,
        final ISchemaType type,
        final Collection<ThriftAnnotation> _annotations
      ) throws SchemaBuilderException {
    super(
      ModuleSchema.class,
      TypedefSchema.class,
      parent,
      new SchemaReference(SchemaReference.Type.TYPEDEF, parent.getName(), name),
      _annotations
    );
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null.");
    }
    this._type = getSchemaContext().wrap(type);
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
  @ThriftField(THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return _type.getProtocolType();
  }


  public static class Builder extends AbstractSchemaBuilder<
      ModuleSchema,
      TypedefSchema,
      ModuleSchema.Builder,
      Builder> {

    public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
      this(null);
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

    Builder(ModuleSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
    }

    private ISchemaType _type;

    public Builder type(ISchemaType type) {
      this._type = type;
      return this;
    }

    @Override
    protected TypedefSchema _build(
        final ModuleSchema _parent) throws SchemaBuilderException {
      super._validate();
      return new TypedefSchema(_parent, getName(), _type, getAnnotations());
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "enumValues" };
    }

    @Override
    @ThriftConstructor
    public TypedefSchema build() throws SchemaBuilderException {
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }
    
  }
}
