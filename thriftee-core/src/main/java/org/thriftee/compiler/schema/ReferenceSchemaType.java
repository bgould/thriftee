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

import org.thriftee.compiler.schema.XMLSchemaBuilder.SchemaContextCreatedListener;

public final class ReferenceSchemaType
    implements ISchemaType, SchemaContextCreatedListener {

  private ISchemaType resolved;

  private final SchemaReference reference;

  public ReferenceSchemaType(final SchemaReference reference) {
    super();
    this.reference = reference;
  }

  @Override
  public void schemaContextCreated(final SchemaContext ctx) {
    if (this.resolved != null) {
      throw new IllegalStateException("reference has already been resolved.");
    }
    this.resolved = ctx.resolveReference(reference);
  }

  @Override
  public String getModuleName() {
    return resolved.getModuleName();
  }

  @Override
  public String getTypeName() {
    return resolved.getTypeName();
  }

  @Override
  public ThriftProtocolType getProtocolType() {
    return resolved().getProtocolType();
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return resolved().toNamespacedIDL(namespace);
  }

  @Override
  public <T extends ISchemaType> T castTo(Class<T> schemaTypeClass) {
    return resolved().castTo(schemaTypeClass);
  }

  private ISchemaType resolved() {
    if (resolved == null) {
      throw new IllegalStateException("schema type has not been resolved.");
    }
    return resolved;
  }

}
