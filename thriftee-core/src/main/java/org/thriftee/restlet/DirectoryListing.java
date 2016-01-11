/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.restlet;

import static org.thriftee.restlet.FrameworkResource.thrift;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryListing extends Directory {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  //private final String base;

  public DirectoryListing(Context context, Reference uri) {
    super(context, uri);
    setListingAllowed(true);
    //this.base = base;
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
      final DirectoryListingModel dir = getDirectoryModel(refList);
      return new DirectoryListingRepresentation(thrift(getContext()), dir);
    } finally {
      LOG.trace("exiting getHtmlRepresentation()");
    }
  }

  protected DirectoryListingModel getDirectoryModel(final ReferenceList rl) {
    LOG.trace("entering getDirectoryModel()");
    try {
      final DirectoryListingModel model = 
          FrameworkResource.createDefaultModel(DirectoryListing.class);
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
      return model;
    } finally {
      LOG.trace("exiting getDirectoryModel()");
    }
  }

  private String normalizeIdentifier(ReferenceList rl) {
    //final String root = FrameworkResource.requestRoot();
    //System.out.println("root: " + root);
    //final String str = getRelativePart(root, rl.getIdentifier().toString());
    //System.out.println("str: " + root);
    //final String rel = getRelativePart(base, str+(str.endsWith("/")?"":"/"));
    return normalize(withTrailingSlash(rl.getIdentifier().toString()));
  }

  private String normalizeReference(Reference ref) {
    //final String refstr = removeBase(ref.toString());
    //return new Reference(getRootRef(), refstr).getTargetRef().toString();
    return normalize(ref.toString());
  }

  private String normalize(final String str) {
    final String refstr = removeBase(str);
    return new Reference(getRootRef(), refstr).getTargetRef().toString();
  }

  private String removeBase(final String refstr) {
    return getRelativePart(FrameworkResource.resourceBaseRef().toString(), refstr);
  }

  private static String withTrailingSlash(final String str) {
    return str + (str.endsWith("/") ? "" : "/");
  }

  private static String getRelativePart(final String base, final String str) {
    if (!str.startsWith(base)) {
      throw new IllegalArgumentException(String.format(
        "str '%s' must start with base '%s'", str, base));
    }
    return str.substring(base.length());
  }

}
