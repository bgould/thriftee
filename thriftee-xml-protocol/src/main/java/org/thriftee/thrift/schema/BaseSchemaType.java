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

abstract class BaseSchemaType<
    P extends BaseSchema<?, ?>, 
    T extends BaseSchema<P, T>
  > extends BaseSchema<P, T> implements SchemaType {

  private static final long serialVersionUID = -4797781153586878306L;
  
  private final SchemaReference reference;
  
  public SchemaReference getReference() {
    return this.reference;
  }

  protected BaseSchemaType(
        final Class<P> parentClass, 
        final Class<T> thisClass, 
        final P parent, 
        final SchemaReference reference,
        final String doc,
        final Collection<ThriftAnnotation> annotations
      ) throws SchemaBuilderException {
    super(
      parentClass, 
      thisClass,
      parent, 
      reference.getTypeName(),
      doc,
      annotations
    );
    this.reference = reference;
  }

  @Override
  public String toNamespacedIDL(final String namespace) {
    return getReference().toNamespacedIDL(namespace);
  }

  @Override
  public <C extends SchemaType> C castTo(final Class<C> schemaTypeClass) {
    return schemaTypeClass.cast(this);
  }
/*
  @Override
  public String getTypeName() {
    // TODO Auto-generated method stub
    return null;
  }
*/
  @Override
  public final SchemaType getTrueType() {
    return this;
  }

}
