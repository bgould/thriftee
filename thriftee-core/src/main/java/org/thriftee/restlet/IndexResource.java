package org.thriftee.restlet;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.thriftee.util.New;

public class IndexResource extends FrameworkResource {

  @Get
  public Representation represent() {
  
    final DirectoryListingModel directory = createDefaultModel();
    directory.getFiles().put("clients/", "clients/");
    directory.getFiles().put("endpoints/", "endpoints/");

    final Map<String, Object> model = New.map();
    model.put("title", directory.getTitle());
    model.put("directory", directory);
    return getTemplate("directory", model, MediaType.TEXT_HTML);

  }

}
