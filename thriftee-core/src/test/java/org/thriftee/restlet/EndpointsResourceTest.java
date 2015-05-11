package org.thriftee.restlet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EndpointsResourceTest extends ResourceTestBase {

  public static final String MODULE = "org_thriftee_examples_presidents";
  
  public static final String SERVICE = "PresidentService";

  @Test public void testGet() {
    this.handleGet("/endpoints/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testGetMultiplex() {
    this.handleGet("/endpoints/multiplex/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testGetMultiplexWithProtocol() {
    this.handleGet("/endpoints/multiplex/json");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testGetMultiplexWithInvalidProtocol() {
    this.handleGet("/endpoints/multiplex/jzon");
    assertEquals(404, rsp().getStatus().getCode());
  }

  @Test public void testInvalidModule() {
    this.handleGet("/endpoints/blah/");
    assertEquals(404, rsp().getStatus().getCode());
  }
  
  @Test public void testValidModule() {
    this.handleGet("/endpoints/" + MODULE + "/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testInvalidService() {
    this.handleGet("/endpoints/" + MODULE + "/blah/");
    assertEquals(404, rsp().getStatus().getCode());
  }
  
  @Test public void testValidService() {
    this.handleGet("/endpoints/" + MODULE + "/" + SERVICE + "/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testInvalidProtocol() {
    this.handleGet("/endpoints/" + MODULE + "/" + SERVICE + "/blah");
    assertEquals(404, rsp().getStatus().getCode());
  }
  
  @Test public void testValidProtocol() {
    this.handleGet("/endpoints/" + MODULE + "/" + SERVICE + "/json");
    assertEquals(200, rsp().getStatus().getCode());
  }
}
