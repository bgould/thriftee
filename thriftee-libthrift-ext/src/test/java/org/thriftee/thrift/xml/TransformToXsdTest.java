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

import java.io.File;
import java.net.URL;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.thrift.xml.protocol.TXMLProtocol.XML;

@RunWith(Parameterized.class)
public class TransformToXsdTest extends BaseThriftXMLTest {

  @Parameters
  public static Collection<Object[]> data() {
    return testParameters();
  }

  private final TestObject testobj;

  public TransformToXsdTest(TestObject testobj) {
    super();
    this.testobj = testobj;
  }

  @Test
  public void testGenerateXsd() throws Exception {
    if (testobj instanceof TestCall) {
      return;
    }
    final File dataFile = testobj.simpleXml();
    final File xsdFile = schemaFor(testobj.module);
    final URL xsdUrl = xsdFile.toURI().toURL();
    System.out.println("\nValidating against XSD: " + xsdUrl + "\n-----------");
    System.out.println(readFileAsString(dataFile));
    String error = XML.validate(xsdUrl, new StreamSource(dataFile));
    if (error == null) {
      System.out.println("----------\nValidation successful.\n----------\n");
    } else {
      Assert.fail(error);
    }
  }

}
