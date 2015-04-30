package org.thriftee.restlet;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

public class DebugResource extends FrameworkResource {

  @Get
  public Representation get() {
    return getDebugTemplate(getModel(), MediaType.TEXT_HTML);
  }

  @Post
  public Representation post() {
    return getDebugTemplate(getModel(), MediaType.TEXT_HTML);
  }
 
  @Put
  public Representation put() {
    return getDebugTemplate(getModel(), MediaType.TEXT_HTML);
  }
  
  @Delete
  public Representation delete() {
    return getDebugTemplate(getModel(), MediaType.TEXT_HTML);
  }

  private Object getModel() {
    Map<String, Object> model = new HashMap<String, Object>();
    return model;
  }

}
