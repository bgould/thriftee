package org.thriftee.restlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.framework.ThriftEE;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftCodecManager;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

public abstract class FrameworkResource extends ServerResource {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  //static final String servletCtxAttr = "org.restlet.ext.servlet.ServletContext";

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
    return StringUtils.trimToEmpty(resourceRef().getRemainingPart());
  }

  public static ThriftEE thrift(final Context ctx) {
    return (ThriftEE) ctx.getAttributes().get(APP_CTX_ATTR);
  }

  protected ThriftEE thrift() {
    return thrift(getContext());
  }
  
  protected ThriftCodecManager codecManager() {
    return thrift().codecManager();
  }

  private static final String prefix = "/org/thriftee/restlet/templates";

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

  public static DirectoryListingModel createDefaultModel(Class<?> forClass) {
    final DirectoryListingModel model = new DirectoryListingModel();
    final String listingPath = resourceBaseRef().getPath();
    if (!listingPath.endsWith("/")) {
      throw new IllegalStateException("listingPath should end with a slash");
    }
    model.setTitle("Index of '" + listingPath  + "'");
    model.setBaseRef(resourceRef().toString());
    if (!(forClass.equals(IndexResource.class))) {
      model.getFiles().put("../", "../");
    }
    return model;
  }

  protected DirectoryListingModel createDefaultModel() {
    return createDefaultModel(getClass());
  }

  protected Representation listing(DirectoryListingModel dirModel) {
    final Map<String, Object> model = New.map();
    model.put("title", dirModel.getTitle());
    model.put("directory", dirModel);
    return getTemplate("directory", model, MediaType.TEXT_HTML);
  }

  protected Representation notFound() {
    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    return null;
  }

  protected TemplateRepresentation getDebugTemplate(
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

  protected void debug(String fmt, Object... args) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format(fmt, (Object[]) args));
    }
  }

}
