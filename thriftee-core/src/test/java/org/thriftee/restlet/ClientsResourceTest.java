package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientsResourceTest extends ResourceTestBase {

  @Test
  public void testGet() {

    assertEquals(4, thrift().clientTypeAliases().size());
    this.handleGet("/clients/");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());
    assertTrue(text.indexOf("Available Thrift Clients") > -1);

  }

  @Test
  public void testGetClientDirListing() {

    this.handleGet("/clients/php/");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());

    final boolean hasTypesPhp = text.indexOf("<a href=\"./Types.php\"") > -1;
    assertTrue("Listing should contain ./Types.php file", hasTypesPhp);
    
    final boolean hasOrgDir = text.indexOf("<a href=\"./org/\"") > -1;
    assertTrue("Listing should have ./org/ directory link", hasOrgDir);

  }

  @Test
  public void testGetClientFile() {

    this.handleGet("/clients/php/Types.php");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());
    assertTrue(text.indexOf("<?php") > -1);

  }

  @Test
  public void testGetClientSecondLevelDirListing() {

    this.handleGet("/clients/php/org/");
    assertEquals(200, rsp().getStatus().getCode());

    final String text = rsp().getEntityAsText();
    LOG.debug("response text:\n{}", rsp().getEntityAsText());
    assertTrue(text.indexOf("thriftee") > -1);

  }

}
