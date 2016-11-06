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

import static org.thriftee.thrift.schema.SchemaBuilderException.ensureNotNull;

import java.io.Serializable;

public abstract class ContainerSchemaType implements SchemaType, Serializable {

  private static final long serialVersionUID = 34730300350398087L;

  private final SchemaType valueType;

  private final ThriftProtocolType protocolType;

  protected ContainerSchemaType(
        final ThriftProtocolType protocolType,
        final SchemaType valueType
      ) throws SchemaBuilderException {
    this.valueType = ensureNotNull("value type", valueType);
    this.protocolType = ensureNotNull("protocol type", protocolType);
  }

  public final SchemaType getValueType() {
    return this.valueType;
  }

  @Override
  public final String getTypeName() {
    return toNamespacedIDL(null);
  }

  @Override
  public final String getModuleName() {
    return "";
  }

  @Override
  public final ThriftProtocolType getProtocolType() {
    return this.protocolType;
  }

  @Override
  public final <T extends SchemaType> T castTo(Class<T> schemaTypeClass) {
    return schemaTypeClass.cast(this);
  }

  @Override
  public final SchemaType getTrueType() {
    return this;
  }

}
