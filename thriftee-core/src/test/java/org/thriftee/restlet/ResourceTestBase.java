package org.thriftee.restlet;

import org.junit.After;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.thriftee.servlet.ThriftEEServlet;
import org.thriftee.tests.AbstractThriftEETest;

public class ResourceTestBase extends AbstractThriftEETest {

  private Component component;

  private ThriftApplication app;

  private Request request;

  private Response response;

  @Before
  public synchronized void setup() throws Exception {
    this.component = new Component();
    ThriftEEServlet.initComponent(this.component);
    this.app = new ThriftApplication();
    //this.app.setContext(this.component.getContext().createChildContext());
    this.component.getDefaultHost().attach(this.app);
    this.app.getContext().getAttributes().put(FrameworkResource._attr2, thrift());
    component.start();
    //this.component.attach("/", this.app);
    //this.app.getContext().getAttributes().put(FrameworkResource._attr2, thrift());
//    context.setAttributes(new HashMap<String, Object>());
//    this.app.setContext(context);
//    app.getContext().getAttributes().put(FrameworkResource._attr2, thrift());
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
    this.request = new Request(Method.GET, uri);
    this.response = new Response(this.request);
    app().handle(request, response);
  }

}
