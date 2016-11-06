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

public final class MethodReturnsSchema extends MethodResultFieldSchema {

  private static final long serialVersionUID = -1297636271308306276L;

  public MethodReturnsSchema(MethodResultSchema _parent, SchemaType _type)
      throws SchemaBuilderException {
    super(_parent, "success", "", null, _type, Requiredness.NONE, (short)0);
  }

  public static class Builder extends MethodResultFieldSchema.Builder {

    protected Builder(MethodResultSchema.Builder parentBuilder) {
      super(parentBuilder);
      name("success");
    }

    @Override
    protected MethodReturnsSchema _buildInstance(MethodResultSchema _parent)
        throws SchemaBuilderException {
      return new MethodReturnsSchema(_parent, getType());
    }

    @Override
    protected String _fieldTypeName() {
      return "result";
    }

  }

}
