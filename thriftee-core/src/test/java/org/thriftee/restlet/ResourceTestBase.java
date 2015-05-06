package org.thriftee.restlet;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
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
    this.component.getDefaultHost().attach(this.app);
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

  public synchronized void handleGet(String uri) {
    LOG.debug("running handleGet() for URI: {}", uri);

    final List<Preference<MediaType>> accepted = New.arrayList();
    accepted.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.9f));

    this.request = new Request(Method.GET, uri);
    this.response = new Response(this.request);
    this.request.getClientInfo().setAcceptedMediaTypes(accepted);

    app().handle(request, response);
    LOG.debug("exiting handleGet()");
  }

}
