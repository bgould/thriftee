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

public final class ExceptionFieldSchema 
    extends AbstractFieldSchema<ExceptionSchema, ExceptionFieldSchema> {

  private ExceptionFieldSchema(
      final ExceptionSchema parent, 
      final String name,
      final String doc,
      final Collection<ThriftAnnotation> annotations,
      final SchemaType type,
      final Requiredness required, 
      final Short identifier) throws SchemaBuilderException {
    super(
      ExceptionSchema.class,
      ExceptionFieldSchema.class,
      parent,
      name,
      doc,
      annotations,
      type,
      required,
      identifier
    );
  }

  private static final long serialVersionUID = 1432035891017906486L;

  public static final class Builder extends 
    AbstractFieldSchema.AbstractFieldBuilder<
      ExceptionSchema,
      ExceptionFieldSchema,
      ExceptionSchema.Builder,
      ExceptionFieldSchema.Builder
    >  {

    Builder(ExceptionSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
    }

    @Override
    protected String _fieldTypeName() {
      return "exception field";
    }

    @Override
    protected ExceptionFieldSchema _buildInstance(final ExceptionSchema parent)
        throws SchemaBuilderException {
      return new ExceptionFieldSchema(
        parent,
        getName(),
        getDoc(),
        getAnnotations(),
        getType(),
        getRequiredness(),
        getIdentifier()
      );
    }

  }

}
