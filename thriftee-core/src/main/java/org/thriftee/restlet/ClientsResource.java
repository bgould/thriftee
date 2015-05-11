package org.thriftee.restlet;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.thriftee.framework.ClientTypeAlias;
import org.thriftee.util.New;

public class ClientsResource extends FrameworkResource {

  @Get
  public Representation get() {

    final DirectoryListingModel directory = createDefaultModel();
    for (final ClientTypeAlias alias : thrift().clientTypeAliases().values()) {
      directory.getFiles().put(alias.getName() + "/", alias.getName() + "/");
    }
 
    final Map<String, Object> model = New.map();
    model.put("title", directory.getTitle());
    model.put("directory", directory);
    return getTemplate("clients", model, MediaType.TEXT_HTML);

  }

}
