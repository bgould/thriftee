package org.thriftee.restlet;

import java.io.File;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.framework.ThriftEE;
import org.thriftee.framework.client.ClientTypeAlias;

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
    final Router router = new Router(getContext());
    attach(router, IndexResource.class, "/");
    attach(router, EndpointsResource.class,
      "/endpoints/",
      "/endpoints/multiplex/",
      "/endpoints/multiplex/{protocol}",
      "/endpoints/{module}/",
      "/endpoints/{module}/{service}/",
      "/endpoints/{module}/{service}/{protocol}"
    );
    router.attach("/clients/", new DirectoryListing(
      getContext().createChildContext(), 
      LocalReference.createFileReference(thrift().clientsDir())
    ));
/*
    attach(router, ClientsResource.class, "/clients/");
    for (final ClientTypeAlias alias : thrift().clientTypeAliases().values()) {
      attach(router, alias);
    }
*/
    return router;
  }

  private ThriftEE thrift() {
    return FrameworkResource.thrift(getContext());
  }

  private void attach(Router r, Class<? extends ServerResource> c, String...p) {
    for (final String path : p) {
      r.attach(path, c);
    }
  }
/*
  private void attach(Router router, ClientTypeAlias alias) {
    final String name = alias.getName();
    final Context ctx = getContext().createChildContext();
    final File zipfile = thrift().clientLibraryZip(name);
    final Reference file = LocalReference.createFileReference(zipfile);
    final Reference zip = LocalReference.createZipReference(file, "");
    final DirectoryListing dir = new DirectoryListing(ctx, zip);
    LOG.trace("attaching client: {} to {}", name, zip);
    router.attach("/clients/" + name + "/", dir);
  }
*/
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

