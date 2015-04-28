package org.thriftee.restlet;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class IndexResource extends FrameworkResource {

  @Get
  public Representation represent() {
    debug("entering");
    Object data = new IndexModel();
    final Representation r = getTemplate("index", data, MediaType.TEXT_HTML);
    debug("exiting");
    return r;
  }

  public class IndexModel {

    private final String title = "API Index Page";

    public String getTitle() {
      return this.title;
    }

  }

}
