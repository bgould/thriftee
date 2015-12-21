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

import com.facebook.swift.codec.ThriftProtocolType;

public class MapSchemaType extends ContainerSchemaType {

  private static final long serialVersionUID = -5613803424652950927L;

  private final ISchemaType keyType;
  
  public MapSchemaType(final ISchemaType _keyType, final ISchemaType _valueType) {
    super(_valueType);
    this.keyType = _keyType;
  }
  
  public ISchemaType getKeyType() {
    return this.keyType;
  }
  
  @Override
  public String getTypeName() {
    return toNamespacedIDL(null);
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return ThriftProtocolType.MAP;
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return "map<" + getKeyType().toNamespacedIDL(namespace) + ", " + getValueType().toNamespacedIDL(namespace) + ">";
  }

}
