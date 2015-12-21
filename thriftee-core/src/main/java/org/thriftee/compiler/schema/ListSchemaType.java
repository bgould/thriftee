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

public class ListSchemaType extends ContainerSchemaType {

  public ListSchemaType(ISchemaType valueType) {
    super(valueType);
  }

  private static final long serialVersionUID = 1896674969956124265L;

  @Override
  public String getTypeName() {
    return toNamespacedIDL(null);
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return ThriftProtocolType.LIST;
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return "list<" + getValueType().toNamespacedIDL(namespace) + ">";
  }

}
