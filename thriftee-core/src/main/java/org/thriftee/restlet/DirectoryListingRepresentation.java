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

import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.restlet.data.MediaType;
import org.thriftee.framework.ThriftEE;

public class DirectoryListingRepresentation extends TransformerRepresentation {

  private final DirectoryListingModel model;

  public DirectoryListingRepresentation(
      final ThriftEE thrift,
      final DirectoryListingModel model) {
    super(MediaType.TEXT_HTML, thrift, templateUrl());
    this.model = model;
  }

  private static URL templateUrl() {
    return DirectoryListingRepresentation.class.getClassLoader().getResource(
      FrameworkResource.XSLT_PREFIX + "directory.xsl"
    );
  }

  @Override
  protected Source source() throws IOException {
    return new DOMSource(this.model.writeToDom(newDocument()));
  }

}
