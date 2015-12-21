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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.junit.Test;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.tests.AbstractThriftEETest;

import com.facebook.swift.codec.ThriftCodec;

public class SchemaBuilderTest extends AbstractThriftEETest {

  public SchemaBuilderTest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testExampleSchema() throws Exception {

    ThriftSchema schema = thrift().schema();
    assertNotNull("schema must not be null", schema);
    assertNotNull("modules collection must not be null", schema.getModules());
    assertTrue("number of modules must be greater than 1", schema.getModules().size() > 1);
    LOG.debug("modules in schema: {}", schema.getModules());

    ModuleSchema classicmodels = schema.getModules().get("org_thriftee_examples_classicmodels");
    assertNotNull("classicmodels module must not be null", classicmodels);
//    assertNotNull("classicmodels module must have enums", presidents.getEnums().size() > 0);
//    LOG.debug("enums in module: {}", presidents.getEnums());

//    EnumSchema sortOrderSchema = presidents.getEnums().get("SortOrder");
//    assertNotNull("presidents module must have sort order", sortOrderSchema);
//    LOG.debug("sort order enum: {}", sortOrderSchema);

    StructSchema customerStruct = classicmodels.getStructs().get("Customer");
    assertNotNull("Customer struct must not be null", customerStruct);
    LOG.debug("customer struct: {}", customerStruct);
    LOG.debug("customer struct protocol type: {}", customerStruct.getProtocolType());
    LOG.debug("fields on customer struct: {}", customerStruct.getFields());

    TMemoryBuffer transport = new TMemoryBuffer(1024 * 10);
    TSimpleJSONProtocol protocol = new TSimpleJSONProtocol(transport);
    ThriftCodec<ThriftSchema> codec = thrift().codecManager().getCodec(ThriftSchema.class);
    codec.write(schema, protocol);

    LOG.debug(transport.toString("UTF-8"));

  }
  
}
