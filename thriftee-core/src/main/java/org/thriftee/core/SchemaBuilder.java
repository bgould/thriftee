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
package org.thriftee.core;

import javax.xml.transform.stream.StreamSource;

import org.thriftee.thrift.schema.IdlSchemaBuilder;
import org.thriftee.thrift.schema.SchemaBuilderException;
import org.thriftee.thrift.schema.ThriftSchema;

public interface SchemaBuilder {

  public abstract ThriftSchema buildSchema(SchemaBuilderConfig config) 
      throws SchemaBuilderException;

  public static class FromXML implements SchemaBuilder {

    @Override
    public ThriftSchema buildSchema(SchemaBuilderConfig config) 
        throws SchemaBuilderException {
      final IdlSchemaBuilder bldr = new IdlSchemaBuilder();
      return bldr.buildFromXml(new StreamSource(config.globalXmlFile()));
    }

  }

}