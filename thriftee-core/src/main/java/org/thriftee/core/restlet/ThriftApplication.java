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

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.core.ThriftEE;

public class ThriftApplication extends Application {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public ThriftApplication() {
    super();
  }

  public ThriftApplication(Context context) {
    super(context);
    getMetadataService().setDefaultMediaType(MediaType.TEXT_PLAIN);
    getMetadataService().addExtension("wsdl", MediaType.TEXT_XML);
    getMetadataService().addExtension("xsd", MediaType.TEXT_XML, true);
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
    attach(router, SOAPResource.class,
      "/soap/",
      "/soap/{module}/",
      "/soap/{module}/{service}/",
      "/soap/{module}/{service}/{filename}"
    );
    router.attach("/clients/", createClientsDirectory());
    router.attach("/idl/", createIdlDirectory());
    return router;
  }

  private DirectoryListing createClientsDirectory() {
    final DirectoryListing dir = new DirectoryListing(
      getContext(), // TODO: createChildContext?
      LocalReference.createFileReference(thrift().clientsDir())
    );
    dir.setIndexName("default.html");
    return dir;
  }

  private DirectoryListing createIdlDirectory() {
    final DirectoryListing dir = new DirectoryListing(
      getContext(), // TODO: createChildContext?
      LocalReference.createFileReference(thrift().idlDir())
    );
    return dir;
  }

  private ThriftEE thrift() {
    return FrameworkResource.thrift(getContext());
  }

  private void attach(Router r, Class<? extends ServerResource> c, String...p) {
    for (final String path : p) {
      r.attach(path, c);
    }
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

}

