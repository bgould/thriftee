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

import org.junit.Test;

public class TransformToWsdl extends BaseThriftXMLTest {

  @Test
  public void testGenerateWsdl() throws Exception {
    final File everythingWsdl = wsdlFor("everything.Universe");
    final URL wsdlUrl = everythingWsdl.toURI().toURL();
    System.out.println("\nGenerated WSDL: " + wsdlUrl + "\n-----------");
    System.out.println(readFileAsString(everythingWsdl));
  }

}
