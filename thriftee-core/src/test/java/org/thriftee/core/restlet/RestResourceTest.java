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
package org.thriftee.core.restlet;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReaderFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import org.apache.thrift.transport.TMemoryBuffer;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.thriftee.examples.Examples;
import org.thriftee.thrift.xml.protocol.SimpleJsonProtocol;

import everything.Universe.grok_args;

public class RestResourceTest extends ResourceTestBase {

  static final SimpleJsonProtocol.Factory fctry = new SimpleJsonProtocol.Factory();

  static final JsonReaderFactory readerFactory = Json.createReaderFactory(null);

  static final JsonGeneratorFactory genFactory = Json.createGeneratorFactory(null);

  @Test
  public void testCall() throws Exception {
    final TMemoryBuffer buffer = new TMemoryBuffer(0);
    final SimpleJsonProtocol proto = fctry.getProtocol(buffer);
    proto.setBaseStruct(
      thrift().schema().
      findService("everything", "Universe").
      findMethod("grok").getArgumentStruct()
    );
    final grok_args args = new grok_args(Examples.everythingStruct());
    args.write(proto);
    final StringRepresentation rep = new StringRepresentation(
      buffer.toString("UTF-8"), MediaType.APPLICATION_JSON
    );
    handlePost("/endpoints/rest/everything/Universe/grok", rep);
    final String response = this.rsp().getEntityAsText();
    final JsonObject obj = readerFactory.createReader(
      new StringReader(response)
    ).readObject();
    assertEquals(42, obj.getInt("success"));
  }

  @Test
  public void testCall2() throws Exception {
    final StringWriter sw = new StringWriter();
    final JsonGenerator gen = genFactory.createGenerator(sw);
    gen.writeStartObject().write("fortyTwo", 42).writeEnd();
    gen.flush();
    LOG.debug("posting call: " + sw.toString());
    final StringRepresentation rep =
        new StringRepresentation(sw.toString(), MediaType.APPLICATION_JSON);
    handlePost("/endpoints/rest/everything/Universe/bang", rep);
    final String response = this.rsp().getEntityAsText();
    final JsonObject obj =
        readerFactory.createReader(new StringReader(response)).readObject();
    assertEquals("default", obj.getJsonObject("success").getString("str"));
  }
}
