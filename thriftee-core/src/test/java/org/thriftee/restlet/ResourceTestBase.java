package org.thriftee.restlet;

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
import org.thriftee.tests.AbstractThriftEETest;
import org.thriftee.util.New;

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

  protected void assertHasLink(String link) {
    final String text = rsp().getEntityAsText();
    final boolean hasLink = text.indexOf("<a href=\"" + link + "\"") > -1;
    Assert.assertTrue("Listing should contain " + link + " link", hasLink);
  }

  public synchronized void handleGet(String uri) {
    LOG.debug("running handleGet() for URI: {}", uri);

    final List<Preference<MediaType>> accepted = New.arrayList();
    accepted.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.9f));

    final String rootRef = "tests://localhost/thriftee/tests";

    this.request = new Request(Method.GET, rootRef + uri);
    this.request.setRootRef(new Reference(rootRef));
    this.request.getResourceRef().setBaseRef(rootRef);
    this.request.getClientInfo().setAcceptedMediaTypes(accepted);
    
    this.response = new Response(this.request);

    app().handle(request, response);
    LOG.debug("exiting handleGet()");
  }

}
