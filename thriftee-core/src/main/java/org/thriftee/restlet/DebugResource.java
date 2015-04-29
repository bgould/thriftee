package org.thriftee.restlet;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class DebugResource extends FrameworkResource {

  @Get
  public Representation represent() {
    return getDebugTemplate(null, MediaType.TEXT_HTML);
  }

}
