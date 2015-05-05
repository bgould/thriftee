package org.thriftee.restlet;

import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.thriftee.util.New;

import static org.thriftee.restlet.FrameworkResource.getTemplate;

public class DirectoryListing extends Directory {

  public DirectoryListing(Context context, String uri) {
    super(context, uri);
  }

  @Override
  public Representation getIndexRepresentation(Variant v, ReferenceList rl) {
    if (!v.getMediaType().equals(MediaType.TEXT_HTML)) {
      return super.getIndexRepresentation(v, rl);
    }
    final DirectoryListingModel directory = getDirectoryModel(rl);
    final Map<String, Object> model = New.map();
    model.put("title", directory.getTitle());
    model.put("directory", directory);
    return getTemplate("directory", model, MediaType.TEXT_HTML);
  }

  private DirectoryListingModel getDirectoryModel(ReferenceList refList) {
    final DirectoryListingModel model = new DirectoryListingModel();
    model.setTitle(getIndexName());
    model.setBaseRef(".");
    final Reference identifier = refList.getIdentifier();
    for (final Reference ref : refList) {
      final String rel = ref.getRelativeRef(identifier).toString();
      model.getFiles().put(rel, rel);
    }
    return model;
  }

}
