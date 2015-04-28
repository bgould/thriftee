package org.thriftee.restlet;

import org.junit.After;
import org.junit.Before;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.thriftee.tests.AbstractThriftEETest;

public class ResourceTestBase extends AbstractThriftEETest {

  private ThriftApplication app;

  private Request request;

  private Response response;

  @Before
  public synchronized void setup() {
    this.app = new ThriftApplication();
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
  public synchronized void teardown() {
    this.app = null;
  }

  public synchronized void handleGet(String uri) {
    this.request = new Request(Method.GET, uri);
    this.response = new Response(this.request);
    app().handle(request, response);
  }

}
