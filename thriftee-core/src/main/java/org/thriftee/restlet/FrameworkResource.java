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
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.framework.ThriftEE;

import com.facebook.swift.codec.ThriftCodecManager;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;

public abstract class FrameworkResource extends ServerResource {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());
  
  static final String servletCtxAttr = "org.restlet.ext.servlet.ServletContext";

  public static final String APP_CTX_ATTR = "org.thriftee.app.attr";
  
  public static final String BASEREF_CTX_ATTR = "org.thriftee.baseRef.attr";

  public static void initComponent(Component component) {
    component.getClients().add(Protocol.CLAP);
    component.getClients().add(Protocol.ZIP);
  }

  public static void initApplication(Application app, ThriftEE thrift) {
    final Context ctx = app.getContext();
    ctx.getAttributes().put(FrameworkResource.APP_CTX_ATTR, thrift);
  }

  public static String requestRoot() {
    final Request req = ThriftApplication.currentRequest();
    if (req != null) {
      final Reference rootRef = req.getRootRef();
      //final Reference hostRef = req.getHostRef();
      //final Reference originalRef = req.getOriginalRef();
      //final Reference resourceRef = req.getResourceRef();
      //LoggerFactory.getLogger(FrameworkResource.class).info(
        //"rootRef: {}\nhostRef: {}\noriginalRef: {}\nresourceRef: {}\n", 
        //rootRef, hostRef, originalRef, resourceRef);
      if (rootRef != null) {
        return StringUtils.trimToEmpty(rootRef.toString());
      } else {
        return "";
      }
    }
    throw new IllegalStateException("current request is not set");
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
