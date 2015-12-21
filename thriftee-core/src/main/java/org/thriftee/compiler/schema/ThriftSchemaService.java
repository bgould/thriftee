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

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface ThriftSchemaService {

  @ThriftMethod
  public ThriftSchema getSchema();
  
  @ThriftMethod
  public String xmlTemplate(@ThriftField(name="type") ThriftSchemaType type);

  @ThriftMethod
  public String xmlCall(@ThriftField(name="methodId") MethodIdentifier method);

  public static class Impl implements ThriftSchemaService {

    private final ThriftSchema schema;

    public Impl(final ThriftSchema schema) {
      this.schema = schema;
    }

    @Override
    public ThriftSchema getSchema() {
      return schema;
    }

    @Override
    public String xmlTemplate(ThriftSchemaType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String xmlCall(MethodIdentifier method) {
      // TODO Auto-generated method stub
      return null;
    }

  }

}
