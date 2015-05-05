package org.thriftee.restlet;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
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
  
  static final String _attr = "org.restlet.ext.servlet.ServletContext";

  public static final String _attr2 = "org.thriftee.app.attr";

  public static ThriftEE thrift(final Context ctx) {
    return (ThriftEE) ctx.getAttributes().get(_attr2);
  }

  protected ThriftEE thrift() {
    return thrift(getContext());
    //    return (ThriftEE) getContext().getAttributes().get(_attr2);
    /*
    final Object servletContext = getContext().getAttributes().get(_attr);
		if (servletContext != null) {
      return ThriftServletContext.servicesFor((ServletContext) servletContext);
    } else {
      return (ThriftEE) getContext().getAttributes().get(_attr2);
    }
    */
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
