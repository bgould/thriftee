package org.thriftee.framework;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.Test;
import org.thriftee.examples.presidents.Name;
import org.thriftee.examples.presidents.President;
import org.thriftee.examples.usergroup.domain.User;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.tests.AbstractThriftEETest;

import com.facebook.swift.codec.ThriftCodec;

public class ThriftEETest extends AbstractThriftEETest {

  public ThriftEETest() throws ThriftStartupException {
    super();
  }

  @Test
  public void testServiceLocator() throws Exception {
    UserService userService = thrift().serviceLocator().locate(thrift(), UserService.class);
    User user = userService.find("aaardvark");
    Assert.assertNotNull("returned user must not be null", true);
  }

  @Test
  public void testWriteStruct() throws Exception {

    ThriftCodec<President> presidentCodec = thrift().codecManager().getCodec(
      President.class
    );
    Assert.assertNotNull(presidentCodec);

    President prez = new President();
    prez.setDied(new Date());
    prez.setBorn(new Date());
    prez.setName(new Name("Some", "F", "Guy"));
    prez.setEducation("Some College");
    prez.setPoliticalParty("New Year's");
    prez.setTerm("4 years");
    prez.setCareer("Party Animal");
    prez.setId(44);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TTransport transport = new TIOStreamTransport(baos);
    TProtocol protocol = new TSimpleJSONProtocol(transport);
    presidentCodec.write(prez, protocol);

    byte[] bytes = baos.toByteArray();
    Assert.assertTrue("byte array with result has length > 0", bytes.length > 0);

    LOG.debug("Serialized object: {}", new String(bytes));

  }

}
