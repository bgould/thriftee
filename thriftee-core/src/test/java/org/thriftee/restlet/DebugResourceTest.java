package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DebugResourceTest extends ResourceTestBase {

  public void testGet() {

    this.handleGet("/debug");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());
    assertTrue(text.indexOf("Debug Template") > -1);

  }

}
