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
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.core.ThriftEE;
import org.thriftee.core.util.Strings;

public abstract class FrameworkResource extends ServerResource {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public static final String APP_CTX_ATTR = "org.thriftee.app.attr";

  public static final String BASEREF_CTX_ATTR = "org.thriftee.baseRef.attr";

  public static void initComponent(Component component) {
    component.getClients().add(Protocol.CLAP);
    component.getClients().add(Protocol.ZIP);
    component.getClients().add(Protocol.FILE);
  }

  public static void initApplication(Application app, ThriftEE thrift) {
    final Context ctx = app.getContext();
    ctx.getAttributes().put(FrameworkResource.APP_CTX_ATTR, thrift);
  }

  public static Request request() {
    final Request req = ThriftApplication.currentRequest();
    if (req != null) {
      return req;
    }
    throw new IllegalStateException("current request is not set");
  }

  public static Reference rootRef() {
    final Reference rootRef = request().getRootRef();
    if (rootRef != null) {
      return rootRef;
    }
    throw new IllegalStateException("rootRef on current request is not set");
  }

  public static Reference resourceRef() {
    final Reference resourceRef = request().getResourceRef();
    if (resourceRef != null) {
      return resourceRef;
    }
    throw new IllegalStateException("resourceRef on current req is not set");
  }

  public static Reference resourceBaseRef() {
    final Reference baseRef = resourceRef().getBaseRef();
    if (baseRef != null) {
      return baseRef;
    }
    throw new IllegalStateException("baseRef on resourceRef is not set");
  }

  public static String resourceRemainingPart() {
    return Strings.trimToEmpty(resourceRef().getRemainingPart());
  }

  public static ThriftEE thrift(final Context ctx) {
    return (ThriftEE) ctx.getAttributes().get(APP_CTX_ATTR);
  }

  protected ThriftEE thrift() {
    return thrift(getContext());
  }

  public static final String XSLT_PREFIX = "org/thriftee/restlet/templates/";

/*

  private static final Configuration cfg = new Configuration();
  static {
    BeansWrapper beansWrapper = (BeansWrapper) ObjectWrapper.BEANS_WRAPPER;
    beansWrapper.setExposeFields(true);
    cfg.setObjectWrapper(beansWrapper);
    cfg.setClassForTemplateLoading(FrameworkResource.class, prefix);
  }

  public static TemplateRepresentation getTemplate(
      final String tpl,
      final Object data,
      final MediaType mediaType) {
    return new TemplateRepresentation(tpl + ".ftl", cfg, data, mediaType);
  }

*/

  public static DirectoryListingModel createDefaultModel(Class<?> forClass) {
    return createDefaultModel(forClass, true);
  }

  public static DirectoryListingModel createDefaultModel(
      final Class<?> forClass,
      final boolean requireSlash) {
    final DirectoryListingModel model = new DirectoryListingModel();
    final String listingPath = resourceBaseRef().getPath();
    if (requireSlash && !listingPath.endsWith("/")) {
      throw new IllegalStateException("listingPath should end with a slash");
    }
    model.setTitle("Index of '" + listingPath  + "'");
    model.setBaseRef(resourceRef().toString());
    if (!(forClass.equals(IndexResource.class))) {
      model.getFiles().put((listingPath.endsWith("/")?"":"/") + "../", "../");
    }
    return model;
  }

  protected DirectoryListingModel createDefaultModel() {
    return createDefaultModel(getClass());
  }

  protected DirectoryListingModel createDefaultModel(boolean requireSlash) {
    return createDefaultModel(getClass(), requireSlash);
  }

  protected Representation listing(DirectoryListingModel dirModel) {
    return new DirectoryListingRepresentation(thrift(), dirModel);
  }

  protected Representation notFound() {
    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    return null;
  }
/*
  protected Representation getDebugTemplate(
      final Object data,
      final MediaType mediaType) {
    final Map<String, Object> model = new HashMap<>();
    model.put("data", data);
    model.put("title", "Debug Template");
    model.put("restlet", this);
    model.put("context", getContext());
    model.put("request", getRequest());
    model.put("response", getResponse());
    return getTemplate("debug", model, mediaType);
  }
*/
  protected void debug(String fmt, Object... args) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format(fmt, args));
    }
  }

}
