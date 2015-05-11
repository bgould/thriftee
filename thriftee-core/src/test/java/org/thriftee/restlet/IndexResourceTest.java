package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IndexResourceTest extends ResourceTestBase {

  @Test
  public void testGet() {
    this.handleGet("/");
    assertEquals(200, rsp().getStatus().getCode());
    assertHasLink("clients/");
    assertHasLink("endpoints/");
  }

}
