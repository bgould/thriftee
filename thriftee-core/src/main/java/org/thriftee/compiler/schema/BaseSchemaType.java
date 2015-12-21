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

import java.util.Collection;

abstract class BaseSchemaType<P extends BaseSchema<?, ?>, T extends BaseSchema<P, T>> extends BaseSchema<P, T> implements ISchemaType {

  private static final long serialVersionUID = -4797781153586878306L;
  
  private final ReferenceSchemaType reference;
  
  public ReferenceSchemaType getReference() {
    return this.reference;
  }

  protected BaseSchemaType(
      Class<P> parentClass, 
      Class<T> thisClass, 
      P parent, 
      ReferenceSchemaType _reference, 
      Collection<ThriftAnnotation> _annotations) throws SchemaBuilderException {
    super(
      parentClass, 
      thisClass,
      parent, 
      _reference.getTypeName(), 
      (Collection<ThriftAnnotation>) _annotations
    );
    this.reference = _reference;
  }

  @Override
  public String toNamespacedIDL(String namespace) {
    return getReference().toNamespacedIDL(namespace);
  }

}
