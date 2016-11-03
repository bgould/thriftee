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

public enum PrimitiveTypeSchema implements SchemaType {

  VOID   ( null                      ),
  BOOL   ( ThriftProtocolType.BOOL   ),
  BYTE   ( ThriftProtocolType.BYTE   ),
  DOUBLE ( ThriftProtocolType.DOUBLE ),
  I16    ( ThriftProtocolType.I16    ),
  I32    ( ThriftProtocolType.I32    ),
  I64    ( ThriftProtocolType.I64    ),
  LIST   ( ThriftProtocolType.LIST   ),
  MAP    ( ThriftProtocolType.MAP    ),
  SET    ( ThriftProtocolType.SET    ),
  STRING ( ThriftProtocolType.STRING ),
  BINARY ( ThriftProtocolType.BINARY ),
  ;

  private final ThriftProtocolType _protocolType;

  private PrimitiveTypeSchema(ThriftProtocolType protocolType) {
    this._protocolType = protocolType;
  }

  public String getModuleName() {
    return null;
  }

  public ThriftProtocolType getProtocolType() {
    return this._protocolType;
  }

  public String getTypeName() {
    return getProtocolType().name().toLowerCase();
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return getTypeName();
  }

  @Override
  public <T extends SchemaType> T castTo(Class<T> schemaTypeClass) {
    return schemaTypeClass.cast(this);
  }

  @Override
  public SchemaType getTrueType() {
    return this;
  }

}
