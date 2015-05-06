package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IndexResourceTest extends ResourceTestBase {

  @Test
  public void testGet() {

    this.handleGet("/");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());
    assertTrue(text.indexOf("API Index") > -1);

    final boolean hasClients = text.indexOf("<a href=\"/clients/\"") > -1;
    assertTrue("Listing should contain clients/ link", hasClients);

    final boolean hasEndpoints = text.indexOf("<a href=\"/endpoints/\"") > -1;
    assertTrue("Listing should contain endpoints/ link", hasEndpoints);

  }

}
