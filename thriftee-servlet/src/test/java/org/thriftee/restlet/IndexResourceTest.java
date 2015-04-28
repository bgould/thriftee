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
    assertTrue(text.indexOf("API Index Page") > -1);

  }

}
