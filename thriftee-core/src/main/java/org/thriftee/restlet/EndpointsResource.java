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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.framework.ProtocolTypeAlias;
import org.thriftee.util.Strings;

public class EndpointsResource extends FrameworkResource {

  private ModuleSchema module;

  private ServiceSchema service;

  private ProtocolTypeAlias protocol;

  private boolean multiplex;
 
  private final static Pattern multiplexPattern = 
      Pattern.compile("/multiplex/([a-zA-Z0-9_]+)?$");

  boolean resolve() {
    this.module = null;
    this.service = null;
    this.protocol = null;
    this.multiplex = false;
    final String path = resourceBaseRef().getPath();
    final Matcher m = multiplexPattern.matcher(path);
    if (m.find()) {
      this.multiplex = true;
      final String protocolAttr = m.group(1);
      if (protocolAttr != null) {
        this.protocol = thrift().protocolTypeAliases().get(protocolAttr);
        return this.protocol != null;
      }
      return true;
    }
    final String moduleAttr = strAttr("module");
    if (moduleAttr != null) {
      this.module = thrift().schema().getModules().get(moduleAttr);
      if (this.module != null) {
        final String serviceAttr = strAttr("service");
        if (serviceAttr != null) {
          this.service = this.module.getServices().get(serviceAttr);
          if (this.service != null) {
            final String protocolAttr = strAttr("protocol");
            if (protocolAttr != null) {
              this.protocol = thrift().protocolTypeAliases().get(protocolAttr);
              return this.protocol != null;
            }
            return true; // no protocol specified
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
  public Representation process(Representation entity) {
    LOG.trace("entering process()");
    if (!resolve() || getProtocolType() == null) {
      return notFound();
    }
    final MediaType mediaType = entity.getMediaType();
    if (LOG.isTraceEnabled()) {
      LOG.trace("service: {}", getService());
      LOG.trace("protocol: {}", getProtocolType().getName());
      LOG.trace("multiplex: {}", isMultiplex());
      LOG.trace("mediaType: {}", mediaType);
    }
    if (!isMultiplex() && getService() == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
      return null;
    }
    final Representation result = new ThriftProcessorRepresentation(
      entity, 
      getInFactory(), 
      getOutFactory(), 
      getProcessor()
    );
    LOG.trace("exiting process()");
    return result;
  }

  @Get
  public Representation represent() {
    if (!resolve()) {
      return notFound();
    }
    if (getProtocolType() != null) {
      return showProtocol();
    } 
    if (isMultiplex()) {
      return listProtocols();
    }
    if (getService() != null) {
      return listProtocols();
    }
    if (getModule() != null) {
      return listServices();
    }
    else {
      return listModules();
    }
  }

  protected Representation listModules() {
    final DirectoryListingModel directory = createDefaultModel();
    directory.getFiles().put("multiplex/", "multiplex/");
    directory.getFiles().put("soap/", "soap/");
    for (ModuleSchema module : thrift().schema().getModules().values()) {
      if (module.getServices().size() > 0) {
        final String moduleName = module.getName() + "/";
        directory.getFiles().put(moduleName, moduleName);
      }
    }
    return listing(directory);
  }

  protected Representation listServices() {
    final DirectoryListingModel directory = createDefaultModel();
    for (ServiceSchema svc : getModule().getServices().values()) {
      final String name = svc.getName() + "/";
      directory.getFiles().put(name, name);
    }
    return listing(directory);
  }

  protected Representation listProtocols() {
    final DirectoryListingModel directory = createDefaultModel();
    for (ProtocolTypeAlias protocol: thrift().protocolTypeAliases().values()) {
      final String name = protocol.getName();
      directory.getFiles().put(name, name);
    }
    return listing(directory);
  }

  protected Representation showProtocol() {
    final JSONObject json = new JSONObject();
    if (isMultiplex()) {
      json.put("multiplex", true);
    } else {
      json.put("module", getModule().getName());
      json.put("service", getService().getName());
    }
    json.put("protocol", getProtocolType().getName());
    final String jsonStr = json.toString();
    return new StringRepresentation(jsonStr, MediaType.APPLICATION_JSON);
  }

  protected Representation showMultiplex() {
    final JSONObject json = new JSONObject();
    json.put("multiplex", "true");
    if (getProtocolType() != null) {
      json.put("protocol", getProtocolType().getName());
    } else {
      json.put("protocol", "binary");
    }
    final String jsonStr = json.toString();
    return new StringRepresentation(jsonStr, MediaType.APPLICATION_JSON);
  }

  private boolean isMultiplex() {
    return this.multiplex;
  }

  private ModuleSchema getModule() {
    return this.module; 
  }

  private ServiceSchema getService() {
    return this.service;
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

  private String strAttr(String attr) {
    return Strings.trimToNull(getRequest().getAttributes().get(attr));
  }

}
