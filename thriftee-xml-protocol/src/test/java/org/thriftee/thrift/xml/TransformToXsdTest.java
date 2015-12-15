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
