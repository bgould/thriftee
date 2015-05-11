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

  public static void dumpCurrentRequest() {
    final Request request = currentRequest();
    if (request != null) {
      final Reference resourceRef = request.getResourceRef();
      final Reference resourceBaseRef;
      final String resourceRemainingPart;
      if (resourceRef == null) {
        resourceBaseRef = null;
        resourceRemainingPart = null;
      } else {
        resourceBaseRef = resourceRef.getBaseRef();
        resourceRemainingPart = resourceRef.getRemainingPart();
      }
      System.err.printf(
        "%nrootRef: %s%n" + 
        "hostRef: %s%n" + 
        "resourceRef: %s%n" + 
        "resourceRef.baseRef: %s%n" +
        "resourceRef.remainingPart: %s%n", 
        request.getRootRef(),
        request.getHostRef(),
        resourceRef,
        resourceBaseRef,
        resourceRemainingPart
      );
    }
  }

  @Override
  public synchronized Restlet createInboundRoot() {

    final Context ctx = getContext();
    final ThriftEE thrift = FrameworkResource.thrift(ctx);

    final Router router = new Router(getContext());
    router.attach("", IndexResource.class);
    router.attach("/", IndexResource.class);
    router.attach("/clients/", ClientsResource.class);
    router.attach("/endpoints/", EndpointsResource.class);
    router.attach("/endpoints/multiplex/", EndpointsResource.class);
    router.attach("/endpoints/multiplex/{protocol}", EndpointsResource.class);
    router.attach("/endpoints/{module}/", EndpointsResource.class);
    router.attach("/endpoints/{module}/{service}/", EndpointsResource.class);
    router.attach("/endpoints/{module}/{service}/{protocol}", EndpointsResource.class);

    // attach the client directories
    for (final ClientTypeAlias alias : thrift.clientTypeAliases().values()) {
      final String name = alias.getName();
      final File zipfile = thrift.clientLibraryZip(name);
      final Reference zip = LocalReference.createFileReference(zipfile);
      final Reference uri = LocalReference.createZipReference(zip, "");
      final DirectoryListing dir = new DirectoryListing(ctx, uri);
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

