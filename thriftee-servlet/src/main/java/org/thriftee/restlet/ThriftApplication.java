package org.thriftee.restlet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/*
 * 
 * @author bcg
 */
public class ThriftApplication extends Application {

  public ThriftApplication() {
    super();
  }

  public ThriftApplication(Context context) {
    super(context);
  }

  @Override
  public synchronized Restlet createInboundRoot() {
    final Router router = new Router(getContext());
    router.attach("", IndexResource.class);
    router.attach("/", IndexResource.class);
    router.attach("/debug", DebugResource.class);
    return router;
  }

}

