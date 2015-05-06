package org.thriftee.restlet;

import java.io.File;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.framework.ClientTypeAlias;
import org.thriftee.framework.ThriftEE;

/*
 * 
 * @author bcg
 */
public class ThriftApplication extends Application {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public ThriftApplication() {
    super();
  }

  public ThriftApplication(Context context) {
    super(context);
  }

  private static final ThreadLocal<Response> currentResponse = new ThreadLocal<>();

  public static Request currentRequest() {
    final Response rsp = currentResponse();
    if (rsp != null) {
      return rsp.getRequest();
    }
    return null;
  }

  public static Response currentResponse() {
    return currentResponse.get();
  }

  @Override
  public synchronized Restlet createInboundRoot() {

    final Context ctx = getContext();
    final ThriftEE thrift = FrameworkResource.thrift(ctx);

    final Router router = new Router(getContext());
    router.attach("", IndexResource.class);
    router.attach("/", IndexResource.class);
    router.attach("/debug", DebugResource.class);
    router.attach("/clients/", ClientsResource.class);
    router.attach("/services/", EndpointsResource.class);
    router.attach("/services/endpoint", EndpointsResource.class);
    router.attach("/services/endpoint/{svcName}", EndpointsResource.class);
    //router.attach("/clients/{typeAlias}/", ClientsResource.class);
    //router.attach("/services/", EndpointsResource.class);
    //router.attach("/services/endpoint/{svcName}/", EndpointsResource.class);
    
    // attach the client directories
    for (final ClientTypeAlias alias : thrift.clientTypeAliases().values()) {
      final String name = alias.getName();
      final String base = "/clients/" + name + "/";
      //final String uri = thrift.clientLibraryDir(name).toURI().toString());
      final File zipfile = thrift.clientLibraryZip(name);
      final Reference zip = LocalReference.createFileReference(zipfile);
      final Reference uri = LocalReference.createZipReference(zip, "");
      final DirectoryListing dir = new DirectoryListing(ctx, uri, base);
      LOG.trace("attaching client: {} to {}", name, uri);
      router.attach("/clients/" + name + "/", dir);
    }

    return router;
  }

  @Override
  public void handle(Request request, Response response) {
    try {
      currentResponse.set(response);
      super.handle(request, response);
    } finally {
      currentResponse.set(null);
    }
  }

}

