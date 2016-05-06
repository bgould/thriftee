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

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * <p>Thrift does no support polymorphism, so to represent a type in IDL
 * it is necessary to have a wrapper class that represents all possible types.</p>
 * @author bcg
 */
@ThriftStruct
final class ThriftSchemaType implements ISchemaType {

  public static final int THRIFT_INDEX_MODULE_NAME = 1;

  public static final int THRIFT_INDEX_TYPE_NAME = THRIFT_INDEX_MODULE_NAME + 1;

  private final ISchemaType schemaType;

  public ThriftSchemaType() throws NoArgConstructorOnlyExistsForSwiftValidationException {
    throw new NoArgConstructorOnlyExistsForSwiftValidationException();
  }

  ThriftSchemaType(SchemaContext ctx, ISchemaType _schemaType) {
    this.schemaType = _schemaType;
  }

  @Override
  @ThriftField(THRIFT_INDEX_MODULE_NAME)
  public String getModuleName() {
    return this.schemaType.getModuleName();
  }

  @Override
  @ThriftField(THRIFT_INDEX_TYPE_NAME)
  public String getTypeName() {
    return this.schemaType.getTypeName();
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return this.schemaType.getProtocolType();
  }

  @Override
  public String toNamespacedIDL(String _namespace) {
    return this.schemaType.toNamespacedIDL(_namespace);
  }

  public String toString() {
    return String.format(
      "ThriftSchemaType[module=%s, typename=%s, protocolType=%s]",
      getModuleName(), getTypeName(), getProtocolType()
    ); 
  }

  @Override
  public <T extends ISchemaType> T castTo(Class<T> schemaTypeClass) {
    return this.schemaType.castTo(schemaTypeClass);
  }

}
