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

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.thriftee.core.restlet.FrameworkResource;
import org.thriftee.core.restlet.ThriftApplication;
import org.thriftee.core.tests.AbstractThriftEETest;
import org.thriftee.core.util.New;

public class ResourceTestBase extends AbstractThriftEETest {

  private Component component;

  private ThriftApplication app;

  private Request request;

  private Response response;

  @Before
  public synchronized void setup() throws Exception {
 
    this.component = new Component();
    FrameworkResource.initComponent(this.component);
 
    this.app = new ThriftApplication();
    //this.component.getDefaultHost().attach(this.app);
    this.component.getDefaultHost().attach("/thriftee/tests", this.app);
    FrameworkResource.initApplication(this.app, thrift());

    component.start();
  
  }

  public synchronized ThriftApplication app() {
    return this.app;
  }

  public synchronized Request req() {
    return this.request;
  }

  public synchronized Response rsp() {
    return this.response;
  }

  @After
  public synchronized void teardown() throws Exception {
    this.component.stop();
    this.app = null;
    this.component = null;
  }

  private static final String ROOT_REF = "tests://localhost/thriftee/tests";
  
  protected void assertHasLink(String link) {
    final String text = rsp().getEntityAsText();
    final String href = req().getResourceRef() + link;
    final boolean hasLink = text.indexOf("<a href=\"" + href + "\"") > -1;
    Assert.assertTrue("Listing should contain " + href + " link", hasLink);
  }

  public synchronized void handleGet(String uri) {
    LOG.debug("running handleGet() for URI: {}", uri);

    final List<Preference<MediaType>> accepted = New.arrayList();
    accepted.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.9f));

    this.request = new Request(Method.GET, ROOT_REF + uri);
    this.request.setRootRef(new Reference(ROOT_REF));
    this.request.getResourceRef().setBaseRef(ROOT_REF);
    this.request.getClientInfo().setAcceptedMediaTypes(accepted);
    
    this.response = new Response(this.request);

    app().handle(request, response);
    LOG.debug(
      "response entity as text:\n------------------\n{}\n------------------\n",
      response.getEntityAsText()
    );

    LOG.debug("exiting handleGet()");
  }

}
