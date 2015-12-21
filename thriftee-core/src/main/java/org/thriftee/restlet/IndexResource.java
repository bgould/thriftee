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
    directory.getFiles().put("idl/", "idl/");
    directory.getFiles().put("soap/", "soap/");

    final Map<String, Object> model = New.map();
    model.put("title", directory.getTitle());
    model.put("directory", directory);
    return getTemplate("directory", model, MediaType.TEXT_HTML);

  }

}
