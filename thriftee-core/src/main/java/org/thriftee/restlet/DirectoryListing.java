package org.thriftee.restlet;

import static org.thriftee.restlet.FrameworkResource.getTemplate;

import java.util.Map;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.util.New;

public class DirectoryListing extends Directory {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final String base;

  public DirectoryListing(Context context, Reference uri, String base) {
    super(context, uri);
    setListingAllowed(true);
    this.base = base;
  }

  @Override
  public Representation getIndexRepresentation(Variant v, ReferenceList rl) {
    LOG.trace("entering getIndexRepresentation()");
    try {
      final MediaType mediaType = v.getMediaType();
      if (MediaType.TEXT_HTML.equals(mediaType)) {
        return getHtmlRepresentation(rl);
      } else {
        return super.getIndexRepresentation(v, rl);
      }
    } finally {
      LOG.trace("exiting getIndexRepresentation()");
    }
  }

  protected Representation getHtmlRepresentation(final ReferenceList refList) {
    LOG.trace("entering getHtmlRepresentation()");
    try { 
      final DirectoryListingModel directory = getDirectoryModel(refList);
      final Map<String, Object> model = New.map();
      model.put("title", directory.getTitle());
      model.put("directory", directory);
      return getTemplate("directory", model, MediaType.TEXT_HTML);
    } finally {
      LOG.trace("exiting getHtmlRepresentation()");
    }
  }

  protected DirectoryListingModel getDirectoryModel(final ReferenceList rl) {
    LOG.trace("entering getDirectoryModel()");
    try {
      final DirectoryListingModel model = new DirectoryListingModel();
      final String dir = normalizeIdentifier(rl);
      for (final Reference r : rl) {
        final String ref = normalizeReference(r);
        final String rel = getRelativePart(dir, ref);
        // LOG.debug("dir: {}\nref:{}\nrel:{}", dir, ref, rel);
        // for non-heirarchical reference lists (i.e., zip files) need to
        // filter out any records that are in subfolders
        final int slashIndex = rel.indexOf('/');
        if (slashIndex > 0 && slashIndex != (rel.length() - 1)) {
          continue;
        } else {
          model.getFiles().put(rel, rel);
        }
      }
      model.setTitle(getIndexName());
      model.setBaseRef(".");
      return model;
    } finally {
      LOG.trace("exiting getDirectoryModel()");
    }
  }

  private String normalizeIdentifier(ReferenceList rl) {
    final String root = FrameworkResource.requestRoot();
    //System.out.println("root: " + root);
    final String str = getRelativePart(root, rl.getIdentifier().toString());
    //System.out.println("str: " + root);
    final String rel = getRelativePart(base, str+(str.endsWith("/")?"":"/"));
    return new Reference(getRootRef(), rel).getTargetRef().toString();
  }

  private String normalizeReference(Reference ref) {
    final String refstr = removeBase(ref);
    return new Reference(getRootRef(), refstr).getTargetRef().toString();
  }

  private String removeBase(final Reference ref) {
    final String root = FrameworkResource.requestRoot();
    final String str = getRelativePart(root, ref.toString());
    return getRelativePart(this.base, str);
  }

  private static String getRelativePart(final String base, final String str) {
    if (!str.startsWith(base)) {
      throw new IllegalArgumentException(String.format(
        "str '%s' must start with base '%s'", str, base));
    }
    return str.substring(base.length());
  }

}
