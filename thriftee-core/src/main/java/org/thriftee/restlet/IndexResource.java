package org.thriftee.restlet;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.thriftee.util.New;

public class IndexResource extends FrameworkResource {

  @Get
  public Representation represent() {
  
    final String title = "API Index";
    final DirectoryListingModel directory = new DirectoryListingModel();
    directory.setTitle(title);
    directory.setBaseRef(FrameworkResource.requestRoot());
    directory.getFiles().put("clients/", "clients/");
    directory.getFiles().put("endpoints/", "endpoints/");

    final Map<String, Object> model = New.map();
    model.put("title", title);
    model.put("directory", directory);
    return getTemplate("directory", model, MediaType.TEXT_HTML);

  }

}
