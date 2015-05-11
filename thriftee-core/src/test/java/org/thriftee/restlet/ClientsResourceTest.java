package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientsResourceTest extends ResourceTestBase {

  @Test
  public void testGetClientDirListing() {
    this.handleGet("/clients/php/");
    assertEquals(200, rsp().getStatus().getCode());
    assertHasLink("Types.php");
    assertHasLink("org/");
  }

  @Test
  public void testGetClientSecondLevelDirListing() {
    this.handleGet("/clients/php/org/");
    assertEquals(200, rsp().getStatus().getCode());
    assertHasLink("thriftee/");
  }

  @Test
  public void testGetClientFile() {
    this.handleGet("/clients/php/Types.php");
    assertEquals(200, rsp().getStatus().getCode());
    assertTrue(rsp().getEntityAsText().indexOf("<?php") > -1);
  }

}
