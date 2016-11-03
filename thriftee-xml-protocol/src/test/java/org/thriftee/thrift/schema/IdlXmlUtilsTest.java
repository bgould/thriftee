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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.thriftee.thrift.schema.idl.IdlBodyDefinition;
import org.thriftee.thrift.schema.idl.IdlDocument;
import org.thriftee.thrift.schema.idl.IdlException;
import org.thriftee.thrift.schema.idl.IdlField;
import org.thriftee.thrift.schema.idl.IdlInclude;
import org.thriftee.thrift.schema.idl.IdlNamespace;
import org.thriftee.thrift.schema.idl.IdlSchema;
import org.thriftee.thrift.schema.idl.IdlTypeIdentifier;
import org.thriftee.thrift.schema.idl.IdlTypedef;

public class IdlXmlUtilsTest extends BaseSchemaTest {

  @Test
  public void testRead() throws Exception {
    final File xmlModel = createXmlModel();

    final IdlSchema schema = IdlXmlUtils.fromXml(new StreamSource(xmlModel));

    assertNotNull("schema must not be null", schema);
    assertNotNull(schema.getDocuments());
    assertEquals(2, schema.getDocuments().size());

    final IdlDocument everythingDoc = schema.getDocuments().get(0);
    assertNotNull("everything module must not be null", everythingDoc);
    assertEquals("everything", everythingDoc.getName());
    assertTrue(everythingDoc.getDoc().startsWith("this is just"));

    final IdlDocument nothingDoc = schema.getDocuments().get(1);
    assertNotNull("nothing_all_at_once module must not be null", everythingDoc);
    assertEquals("nothing_all_at_once", nothingDoc.getName());

    assertEquals(3, everythingDoc.getHeader().size());

    final IdlInclude inc = getInclude(everythingDoc, 0);
    assertNotNull(inc);
    assertEquals("nothing_all_at_once", inc.getName());

    final IdlNamespace ns1 = getNamespace(everythingDoc, 1);
    assertEquals("java", ns1.getName());
    assertEquals("everything", ns1.getValue());

    final IdlNamespace ns2 = getNamespace(everythingDoc, 2);
    assertEquals("xml", ns2.getName());
    assertEquals("evrything", ns2.getValue());
    assertFalse(ns2.getAnnotations().isEmpty());

    final List<IdlTypedef> typedefs = getDefs(everythingDoc, IdlTypedef.class);
    assertEquals(9, typedefs.size());

    final IdlTypedef dukk = typedefs.get(0);
    assertEquals("dukk", dukk.getName());
    assertEquals(IdlTypeIdentifier.I32, dukk.getType().getType());

    final IdlTypedef int32 = typedefs.get(1);
    assertEquals("int32", int32.getName());
    assertEquals(IdlTypeIdentifier.I32, int32.getType().getType());

    final IdlTypedef poig = typedefs.get(2);
    assertEquals("poig", poig.getName());
    assertEquals(IdlTypeIdentifier.UNION, poig.getType().getType());
    assertEquals("everything", poig.getType().getTypeModule());
    assertEquals("Sprat", poig.getType().getTypeId());

    final IdlTypedef plorp = typedefs.get(3);
    assertEquals("plorp", plorp.getName());
    assertEquals(IdlTypeIdentifier.STRUCT, plorp.getType().getType());
    assertEquals("everything", plorp.getType().getTypeModule());
    assertEquals("Spirfle", plorp.getType().getTypeId());

    final IdlTypedef hammlegaff = typedefs.get(4);
    assertEquals("hammlegaff", hammlegaff.getName());
    assertEquals(IdlTypeIdentifier.STRUCT, hammlegaff.getType().getType());
    assertEquals("nothing_all_at_once", hammlegaff.getType().getTypeModule());
    assertEquals("Blotto", hammlegaff.getType().getTypeId());
    assertTrue(hammlegaff.getDoc().startsWith("this goes with a typedef"));

    final List<IdlException> exs = getDefs(everythingDoc, IdlException.class);
    assertEquals(3, exs.size());

    final IdlException endOfUniverse = exs.get(0);
    assertEquals("EndOfTheUniverseException", endOfUniverse.getName());
    assertEquals("trying out an exception", endOfUniverse.getDoc());
    assertEquals(2, endOfUniverse.getAnnotations().size());
    assertEquals("ex.annot", endOfUniverse.getAnnotations().get(0).getKey());
    assertEquals("true", endOfUniverse.getAnnotations().get(0).getValue());
    assertEquals(1, endOfUniverse.getFields().size());

    final IdlField exMsg = endOfUniverse.getFields().get(0);
    assertEquals("msg", exMsg.getName());
    assertEquals(IdlTypeIdentifier.STRING, exMsg.getType().getType());

  }

  public IdlInclude getInclude(IdlDocument doc, int index) {
    return (IdlInclude) doc.getHeader().get(index).getFieldValue();
  }

  public IdlNamespace getNamespace(IdlDocument doc, int index) {
    return (IdlNamespace) doc.getHeader().get(index).getFieldValue();
  }

  public <T> List<T> getDefs(IdlDocument doc, Class<T> type) {
    final List<T> result = new ArrayList<>();
    for (IdlBodyDefinition def : doc.getDefinitions()) {
      Object val = def.getFieldValue();
      if (val != null) {
        if (type.isAssignableFrom(val.getClass())) {
          result.add(type.cast(val));
        }
      }
    }
    return result;
  }
}