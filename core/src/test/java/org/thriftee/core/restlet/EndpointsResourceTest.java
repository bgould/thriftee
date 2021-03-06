/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.core.restlet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EndpointsResourceTest extends ResourceTestBase {

  public static final String MODULE = "org_thriftee_examples_usergroup_service";

  public static final String SERVICE = "UserService";

  @Test public void testGet() {
    this.handleGet("/endpoints/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testInvalidEndpointType() {
    this.handleGet("/endpoints/zorp/");
    assertEquals(404, rsp().getStatus().getCode());
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
    this.handleGet("/endpoints/processor/blah/");
    assertEquals(404, rsp().getStatus().getCode());
  }

  @Test public void testValidModule() {
    this.handleGet("/endpoints/processor/" + MODULE + "/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testInvalidService() {
    this.handleGet("/endpoints/processor/" + MODULE + "/blah/");
    assertEquals(404, rsp().getStatus().getCode());
  }

  @Test public void testValidService() {
    this.handleGet("/endpoints/processor/" + MODULE + "/" + SERVICE + "/");
    assertEquals(200, rsp().getStatus().getCode());
  }

  @Test public void testInvalidProtocol() {
    this.handleGet("/endpoints/processor/" + MODULE + "/" + SERVICE + "/blah");
    assertEquals(404, rsp().getStatus().getCode());
  }

  @Test public void testValidProtocol() {
    this.handleGet("/endpoints/processor/" + MODULE + "/" + SERVICE + "/json");
    assertEquals(200, rsp().getStatus().getCode());
  }
}
