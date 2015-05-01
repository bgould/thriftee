package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientsResourceTest extends ResourceTestBase {

  @Test
  public void testGet() {

    assertEquals(4, thrift().clientTypeAliases().size());
    this.handleGet("/clients");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());
    assertTrue(text.indexOf("Available Thrift Clients") > -1);

  }

}
