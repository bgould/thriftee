package org.thriftee.restlet;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.thriftee.framework.ClientTypeAlias;
import org.thriftee.framework.ThriftEE;

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

    final ThriftEE thrift = FrameworkResource.thrift(getContext());

    final Router router = new Router(getContext());
    router.attach("", IndexResource.class);
    router.attach("/", IndexResource.class);
    router.attach("/debug", DebugResource.class);
    //router.attach("/clients", ClientsResource.class);
    router.attach("/clients/", ClientsResource.class);
    //router.attach("/clients/{typeAlias}", ClientsResource.class);
    //router.attach("/clients/{typeAlias}/", ClientsResource.class);
    router.attach("/services", EndpointsResource.class);
    router.attach("/services/endpoint", EndpointsResource.class);
    router.attach("/services/endpoint/{svcName}", EndpointsResource.class);
    //router.attach("/clients/{typeAlias}/", ClientsResource.class);
    //router.attach("/services/", EndpointsResource.class);
    //router.attach("/services/endpoint/{svcName}/", EndpointsResource.class);
    
    // attach the client directories
    for (final ClientTypeAlias alias : thrift.clientTypeAliases().values()) {
      final String name = alias.getName();
      final String uri = thrift.clientLibraryDir(name).toURI().toString();
      final Directory dir = new Directory(getContext(), uri);
      dir.setListingAllowed(true);
      router.attach("/clients/"+name+"/", dir);
    }

    return router;
  }

}

