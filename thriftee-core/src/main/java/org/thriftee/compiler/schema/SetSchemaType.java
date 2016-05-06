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

public class SetSchemaType extends ContainerSchemaType {

  private static final long serialVersionUID = 7582879752786630514L;

  SetSchemaType(final ISchemaType valueType) {
    super(ThriftProtocolType.SET, valueType);
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return "set<" + getValueType().toNamespacedIDL(namespace) + ">";
  }

}
