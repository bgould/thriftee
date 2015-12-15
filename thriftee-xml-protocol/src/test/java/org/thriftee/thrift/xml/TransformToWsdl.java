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
