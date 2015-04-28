package org.thriftee.restlet;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;

public abstract class FrameworkResource extends ServerResource {

  private Logger LOG = LoggerFactory.getLogger(getClass());

  private static final String prefix = "/org/thriftee/restlet/templates";

  private static final Configuration cfg = new Configuration();
  static {
    cfg.setClassForTemplateLoading(FrameworkResource.class, prefix);
  }
  
  protected TemplateRepresentation getTemplate(
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
    model.put("resource", this);
    return getTemplate("debug", model, mediaType); 
  }
  
  protected void debug(String fmt, Object... args) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format(fmt, (Object[]) args));
    }
  }

}
