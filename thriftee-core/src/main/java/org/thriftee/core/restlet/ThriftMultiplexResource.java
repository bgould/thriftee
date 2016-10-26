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
package org.thriftee.core.restlet;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.thriftee.core.ProtocolTypeAlias;
import org.thriftee.core.util.Strings;

public class ThriftMultiplexResource extends FrameworkResource {

  private ProtocolTypeAlias protocol;

  final boolean resolve() {
    // clear state
    this.protocol = null;

    // if the protocol url part is specified, must match a registered alias
    final String protocolAttr = strAttr("protocol");
    if (protocolAttr != null) {
      this.protocol = thrift().protocolTypeAliases().get(protocolAttr);
      return this.protocol != null;
    }

    // if the protocol part is not specified, delegate to parent
    return true;
  }

  @Post
  public final Representation process(Representation entity) throws IOException {
    LOG.trace("entering process()");
    if (!resolve()) {
      return notFound();
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("mediaType: {}", entity.getMediaType());
    }
    final Representation result = processorFor(entity);
    if (result == null) {
      return notFound();
    } else {
      LOG.trace("exiting process()");
      return result;
    }
  }

  @Get
  public final Representation represent() {
    if (!resolve()) {
      return notFound();
    }
    if (getProtocolType() != null) {
      return showProtocol();
    } else {
      return listProtocols();
    }
  }

  private ThriftProcessorRepresentation processorFor(Representation entity)
      throws IOException {
    if (getProtocolType() == null) {
      return null;
    }
    return new ThriftProcessorRepresentation(
      entity.getMediaType(),
      entity.getStream(),
      getProtocolType().getInFactory(),
      getProtocolType().getOutFactory(),
      thrift().multiplexedProcessor()
    );
  }

  protected final Representation listProtocols() {
    final DirectoryListingModel directory = createDefaultModel();
    for (ProtocolTypeAlias protocol: thrift().protocolTypeAliases().values()) {
      final String name = protocol.getName();
      directory.getFiles().put(name, name);
    }
    return listing(directory);
  }

  protected final Representation showProtocol() {
    final String jsonStr = "{\"id\":\"" + getProtocolType().getName() + "\"}";
    return new StringRepresentation(jsonStr, MediaType.APPLICATION_JSON);
  }

  protected final ProtocolTypeAlias getProtocolType() {
    return this.protocol;
  }


  protected final String strAttr(String attr) {
    return Strings.trimToNull(getRequest().getAttributes().get(attr));
  }

}
