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

package org.thriftee.core.service;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TException;
import org.thriftee.core.ThriftEE;
import org.thriftee.meta.idl.ThriftSchemaService;
import org.thriftee.thrift.schema.IdlXmlUtils;
import org.thriftee.thrift.schema.SchemaBuilderException;
import org.thriftee.thrift.schema.idl.IdlSchema;

public class ThriftSchemaServiceImpl implements ThriftSchemaService.Iface {

  private final IdlSchema _schemaDef;

  public ThriftSchemaServiceImpl(final ThriftEE thrift)
      throws SchemaBuilderException {
    final StreamSource src = new StreamSource(thrift.globalXmlFile());
    this._schemaDef = IdlXmlUtils.fromXml(src);
  }

  @Override
  public IdlSchema getSchema() throws TException {
    return _schemaDef.deepCopy();
  }

}
