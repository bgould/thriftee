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

import org.apache.thrift.TProcessor;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.core.util.Strings;

public abstract class AbstractProcessorResource extends FrameworkResource {

  private ModuleSchema _module;

  private ServiceSchema _service;

  private boolean _resolved = false;

  final boolean resolve() {
    this._module = null;
    this._service = null;
    this._resolved = false;
    final String moduleAttr = strAttr("module");
    if (moduleAttr != null) {
      this._module = thrift().schema().getModules().get(moduleAttr);
      if (this._module != null) {
        final String serviceAttr = strAttr("service");
        if (serviceAttr != null) {
          this._service = this._module.getServices().get(serviceAttr);
          if (this._service != null) {
            this._resolved = resolveRemaining();
            return _resolved;
          }
          return false; // invalid service specified
        }
        return true;   // no service specified
      }
      return false;   // invalid module specified
    }
    return true;     // no module specified
  }

  @Post
  public final Representation process(Representation entity) throws IOException {
    LOG.trace("entering process()");
    if (!resolve()) {
      return notFound();
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("module:    {}", getModule());
      LOG.trace("service:   {}", getService());
      LOG.trace("mediaType: {}", entity.getMediaType());
    }
    if (getService() == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
      return null;
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
    if (resolved()) {
      final Representation representation = getRepresentation();
      if (representation == null) {
        return notFound();
      } else {
        return representation;
      }
    }
    if (getModule() != null) {
      return listServices();
    } else {
      return listModules();
    }
  }

  abstract boolean resolveRemaining();

  abstract AbstractProcessorRepresentation processorFor(Representation entity)
    throws IOException;

  abstract Representation getRepresentation();

  protected final String strAttr(String attr) {
    return Strings.trimToNull(getRequest().getAttributes().get(attr));
  }

  final ModuleSchema getModule() {
    return this._module;
  }

  final ServiceSchema getService() {
    return this._service;
  }

  final TProcessor getProcessor() {
    return thrift().processorFor(getService());
  }

  private final Representation listModules() {
    final DirectoryListingModel directory = createDefaultModel();
    for (ModuleSchema module : thrift().schema().getModules().values()) {
      if (module.getServices().size() > 0) {
        final String moduleName = module.getName() + "/";
        directory.getFiles().put(moduleName, moduleName);
      }
    }
    return listing(directory);
  }

  private Representation listServices() {
    final DirectoryListingModel directory = createDefaultModel();
    for (ServiceSchema svc : getModule().getServices().values()) {
      final String name = svc.getName() + "/";
      directory.getFiles().put(name, name);
    }
    return listing(directory);
  }

  private boolean resolved() {
    return _resolved;
  }
/*
  protected Representation listProtocols() {
    final DirectoryListingModel directory = createDefaultModel();
    for (ProtocolTypeAlias protocol: thrift().protocolTypeAliases().values()) {
      final String name = protocol.getName();
      directory.getFiles().put(name, name);
    }
    return listing(directory);
  }

  protected Representation showProtocol() {
//    final JSONObject json = new JSONObject();
//    if (isMultiplex()) {
//      json.put("multiplex", true);
//    } else {
//      json.put("module", getModule().getName());
//      json.put("service", getService().getName());
//    }
//    json.put("protocol", getProtocolType().getName());
//    final String jsonStr = json.toString();
    final String jsonStr = "{}";
    return new StringRepresentation(jsonStr, MediaType.APPLICATION_JSON);
  }

  protected Representation showMultiplex() {
//    final JSONObject json = new JSONObject();
//    json.put("multiplex", "true");
//    if (getProtocolType() != null) {
//      json.put("protocol", getProtocolType().getName());
//    } else {
//      json.put("protocol", "binary");
//    }
//    final String jsonStr = json.toString();
    final String jsonStr = "{}";
    return new StringRepresentation(jsonStr, MediaType.APPLICATION_JSON);
  }


  private ProtocolTypeAlias getProtocolType() {
    return this.protocol;
  }

  private TProtocolFactory getInFactory() {
    if (getProtocolType() == null) {
      return new TBinaryProtocol.Factory();
    } else {
      return getProtocolType().getInFactory();
    }
  }

  private TProtocolFactory getOutFactory() {
    if (getProtocolType() == null) {
      return new TBinaryProtocol.Factory();
    } else {
      return getProtocolType().getOutFactory();
    }
  }

  private TProcessor getProcessor() {
    if (isMultiplex()) {
      return thrift().multiplexedProcessor();
    } else if (getService() != null) {
      return thrift().processorFor(getService());
    } else {
      throw new IllegalStateException();
    }
  }

*/

}
