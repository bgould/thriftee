package org.thriftee.framework;

import java.io.ByteArrayOutputStream;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.Test;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.examples.classicmodels.Customer;
import org.thriftee.examples.usergroup.domain.User;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.tests.AbstractThriftEETest;

import com.facebook.swift.codec.ThriftCodec;

public class ThriftEETest extends AbstractThriftEETest {

  public static final String MODULE = USERGROUP_SERVICES_MODULE;

  public ThriftEETest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testServiceLocator() throws Exception {
    UserService userService;
    userService = thrift().serviceLocator().locate(UserService.class);
    User aardvark = userService.find("aaardvark");
    Assert.assertNotNull("returned user must not be null", aardvark);
  }

  @Test
  public void testModuleName() throws Exception {
    final String packageName = GroupService.class.getPackage().getName();
    final String moduleName = ThriftEE.moduleNameFor(packageName);
    Assert.assertEquals(MODULE, moduleName);
  }

  @Test
  public void testServiceName() throws Exception {
    final String serviceName = ThriftEE.serviceNameFor(GroupService.class);
    final String expected = MODULE + ".GroupService";
    Assert.assertEquals(expected, serviceName);
  }

  @Test
  public void testProcessorLookup() throws Exception {
    ModuleSchema moduleSchema = thrift().schema().getModules().get(MODULE);
    ServiceSchema svcSchema = moduleSchema.getServices().get("GroupService");
    TProcessor groupService = thrift().processorFor(svcSchema);
    Assert.assertNotNull("TProcessor should not be null", groupService);
  }

  @Test
  public void testMultiplexProcessor() throws Exception {
    TMultiplexedProcessor mp = thrift().multiplexedProcessor();
    LOG.debug("multiplexed processor: {}", mp);
  }

  @Test
  public void testWriteStruct() throws Exception {

    ThriftCodec<Customer> customerCodec = thrift().codecManager().getCodec(
      Customer.class
    );
    Assert.assertNotNull(customerCodec);

    Customer cust = new Customer();
    cust.setCustomerNumber(1);
    cust.setCustomerName("Some F Guy");
    cust.setPhone("555-555-5555");
    cust.setAddressLine1("1234 Main St.");
    cust.setAddressLine2("Suite 987");
    cust.setCity("Anytown");
    cust.setState("NY");
    cust.setCountry("US");
    cust.setPostalCode("12345");
    cust.setCreditLimit(1000);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TTransport transport = new TIOStreamTransport(baos);
    TProtocol protocol = new TSimpleJSONProtocol(transport);
    customerCodec.write(cust, protocol);

    byte[] bytes = baos.toByteArray();
    Assert.assertTrue("byte array with result has length > 0", bytes.length > 0);

    LOG.debug("Serialized object: {}", new String(bytes));

  }

}
