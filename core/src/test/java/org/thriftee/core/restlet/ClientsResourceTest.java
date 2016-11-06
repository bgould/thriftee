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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientsResourceTest extends ResourceTestBase {

  @Test
  public void testGetClientDirListing() {
    this.handleGet("/clients/php/");
    assertEquals(200, rsp().getStatus().getCode());
    assertHasLink("Thrift/");
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
    this.handleGet("/clients/php/Thrift/TMultiplexedProcessor.php");
    assertEquals(200, rsp().getStatus().getCode());
    assertTrue(rsp().getEntityAsText().indexOf("<?php") > -1);
  }

  @Override
  protected boolean generateClients() {
    return true;
  }

}
