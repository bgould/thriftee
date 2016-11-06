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

public final class MethodThrowsSchema extends MethodResultFieldSchema {

  private static final long serialVersionUID = -1297636271308306276L;

  public MethodThrowsSchema(
      final MethodResultSchema parent,
        final String name,
        final String doc,
        final Collection<ThriftAnnotation> annotations,
        final SchemaType type,
        final Requiredness required,
        final Short identifier
      ) throws SchemaBuilderException {
    super(parent, name, doc, annotations, type, required, identifier);
  }

  public static final class Builder extends MethodResultFieldSchema.Builder {

    protected Builder(MethodResultSchema.Builder parentBuilder) {
      super(parentBuilder);
    }

    @Override
    protected MethodThrowsSchema _buildInstance(MethodResultSchema _parent)
        throws SchemaBuilderException {
      return new MethodThrowsSchema(
        _parent,
        getName(),
        getDoc(),
        getAnnotations(),
        getType(),
        getRequiredness(),
        getIdentifier()
      );
    }

    @Override
    protected String _fieldTypeName() {
      return "thrown exception";
    }

  }

}
