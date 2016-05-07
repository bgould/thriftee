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

import static org.junit.Assert.*;

import org.junit.Test;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.tests.AbstractThriftEETest;

public class SchemaBuilderTest extends AbstractThriftEETest {

  @Override
  protected boolean generateClients() {
    return false;
  }

  @Test
  public void testExampleSchema() throws Exception {

    ThriftSchema schema = thrift().schema();
    assertNotNull("schema must not be null", schema);
    assertNotNull("modules collection must not be null", schema.getModules());
    assertTrue(
      "number of modules must be greater than 1", 
      schema.getModules().size() > 1
    );
    LOG.debug("modules in schema: {}", schema.getModules());

    ModuleSchema module = schema.getModules().get("everything");
    assertNotNull("everything module must not be null", module);

    ServiceSchema service = module.getServices().get("Universe");
    assertNotNull("universe service should not be null");
    assertEquals("nothing_all_at_once.Metaverse", service.getParentService());
    assertNotNull(service.getParentServiceSchema());

    MethodSchema method = service.findMethod("grok");
    assertNotNull(method);
    assertNotNull(method.getReturnType());

//    assertNotNull("classicmodels module must have enums", presidents.getEnums().size() > 0);
//    LOG.debug("enums in module: {}", presidents.getEnums());

//    EnumSchema sortOrderSchema = presidents.getEnums().get("SortOrder");
//    assertNotNull("presidents module must have sort order", sortOrderSchema);
//    LOG.debug("sort order enum: {}", sortOrderSchema);

    StructSchema everythingStruct = module.getStructs().get("Everything");
    assertNotNull("Everything struct must not be null", everythingStruct);
    LOG.debug("everything struct: {}", everythingStruct);
    LOG.debug("everything struct protocol type: {}", everythingStruct.getProtocolType());
    LOG.debug("fields on everything struct: {}", everythingStruct.getFields());

  }

}
