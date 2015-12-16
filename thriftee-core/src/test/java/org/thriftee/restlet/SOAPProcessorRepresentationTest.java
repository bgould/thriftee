package org.thriftee.restlet;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.tests.AbstractThriftEETest;

public class SOAPProcessorRepresentationTest extends AbstractThriftEETest {

  private static final String SOAP_REQUEST_1 = 
    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " + 
    " xmlns:gro=\"http://thrift.apache.org/ns/org_thriftee_examples_usergroup_service/GroupService\">" +
    "<soapenv:Header/>" +
    "<soapenv:Body>" +
    "   <gro:findRequest>" +
    "      <arg0>Mammals</arg0>" +
    "   </gro:findRequest>" +
    "</soapenv:Body>" +
    "</soapenv:Envelope>";

  @Test
  public void testProcessor() throws Exception {

    final String modName = USERGROUP_SERVICES_MODULE;
    final ModuleSchema module = thrift().schema().getModules().get(modName);
    final ServiceSchema service = module.getServices().get("GroupService");

    LOG.debug("service call: {}", SOAP_REQUEST_1);
    final byte[] serviceCall = SOAP_REQUEST_1.getBytes();

    final Representation in = new ByteArrayRepresentation(serviceCall);
    final SOAPProcessorRepresentation r = new SOAPProcessorRepresentation(
      in, 
      thrift().globalXmlFile(), 
      modName, 
      service.getName(), 
      thrift().xmlTransforms(), 
      thrift().processorFor(service)
    );
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    r.write(out);
    String resultStr = new String(out.toByteArray());
    LOG.debug("service result: {}", resultStr);
    Assert.assertTrue(resultStr.trim().length() > 0);

  }

}
