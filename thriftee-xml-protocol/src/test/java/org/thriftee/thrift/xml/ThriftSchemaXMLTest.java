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
package org.thriftee.thrift.xml;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamResult;

import org.thriftee.thrift.xml.protocol.TXMLProtocol;
import org.xml.sax.SAXException;

public class ThriftSchemaXMLTest {

  public static final Charset charset = Charset.forName("UTF-8");

  public static final File idlDir = new File("src/test/thrift");

  public static final File everything = new File(idlDir, "everything.thrift");

  public static final File another = new File(idlDir, "nothing_all_at_once.thrift");

  //@Test
  public void testParse() throws IOException, XMLStreamException, SAXException {
    ThriftSchemaXML xml = new ThriftSchemaXML();
    StringWriter out = new StringWriter();
    xml.export(everything, charset, new StreamResult(out));
    final String xmlString = TXMLProtocol.XML.formatXml(out.toString());
    System.out.println(xmlString);

    System.out.println("Validating against schema: " + xml.schemaUrl());
    final String validationError = xml.validate(xmlString);
    if (validationError != null) {
      System.out.println(" invalid!\n" + validationError + "\n");
      fail(validationError);
    }
    System.out.println(" valid.\n\n");
  }

}
