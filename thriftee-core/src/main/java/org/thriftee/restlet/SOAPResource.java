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

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.thrift.TProcessor;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.util.Strings;

// TODO: reorganize to de-dup with EndpointsResource
public class SOAPResource extends FrameworkResource {

  private SortedSet<String> _schemaFilenames;

  private String filename;

  private ModuleSchema module;

  private ServiceSchema service;

  boolean resolve() {
    this.module = null;
    this.service = null;
    final String moduleAttr = strAttr("module");
    if (moduleAttr != null) {
      this.module = thrift().schema().getModules().get(moduleAttr);
      if (this.module != null) {
        final String serviceAttr = strAttr("service");
        if (serviceAttr != null) {
          this.service = this.module.getServices().get(serviceAttr);
          if (this.service != null) {
            this.filename = strAttr("filename");
            if (this.filename != null) {
              return this.filename.equals("service.wsdl") || 
                     getSchemaFilenames().contains(this.filename);
            }
            return true; // no filename specified
          }
          return false; // invalid service specified
        }
        return true;   // no service specified
      }
      return false;   // invalid module specified
    } 
    return true;     // no module specified
  }

  @Get
  public Representation represent() {
    if (!resolve()) {
      return notFound();
    }
    if (getFilename() != null) {
      return showFile();
    }
    if (getService() != null) {
      return listFiles();
    }
    if (getModule() != null) {
      return listServices();
    }
    else {
      return listModules();
    }
  }

  @Post
  public Representation process(Representation entity) {
    LOG.trace("entering process()");
    if (!resolve()) {
      return notFound();
    }
    if (getService() == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
      return null;
    }
    final Representation result = new SOAPProcessorRepresentation(
      entity, 
      thrift().globalXmlFile(),
      getModule().getName(),
      getService().getName(),
      thrift().xmlTransforms(),
      getProcessor()
    );
    LOG.trace("exiting process()");
    return result;
  }

  protected Representation listModules() {
    final DirectoryListingModel directory = createDefaultModel();
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

  protected Representation listFiles() {
    final DirectoryListingModel directory = createDefaultModel();
    directory.getFiles().put("service.wsdl", "service.wsdl");
    for (final String xsdfile : getSchemaFilenames()) {
      directory.getFiles().put(xsdfile, xsdfile);
    }
    return listing(directory);
  }

  protected Representation showFile() {
    if (getFilename() == null) {
      throw new IllegalStateException("getFilename() should not be null");
    }
    final String filename;
    if ("service.wsdl".equals(getFilename())) {
      return new WsdlRepresentation(
        thrift(), fileFor(getWsdlFilename()), 
        resourceRef().toString().replaceAll("service\\.wsdl$", "")
      );
    } else {
      filename = getFilename();
      return new FileRepresentation(fileFor(filename), MediaType.TEXT_XML);
    }
  }

  private final File fileFor(String filename) {
    return new File(thrift().wsdlClientDir(), filename);
  }

  private String getWsdlFilename() {
    return getModule().getName() + "." + getService().getName() + ".wsdl";
  }

  private SortedSet<String> getSchemaFilenames() {
    if (_schemaFilenames == null) {
      if (getModule() == null) {
        throw new IllegalStateException("getModule() should not be null");
      }
      final TreeSet<String> result = new TreeSet<String>();
      result.add(getModule().getName() + ".xsd");
      for (String include : getModule().getIncludes()) {
        if (include.endsWith(".thrift")) {
          include = include.substring(0, include.length() - 7);
        }
        result.add(include + ".xsd");
      }
      this._schemaFilenames = result;
    }
    return _schemaFilenames;
  }

  private String getFilename() {
    return this.filename;
  }

  private ModuleSchema getModule() {
    return this.module; 
  }

  private ServiceSchema getService() {
    return this.service;
  }

  private TProcessor getProcessor() {
    if (getService() != null) {
      return thrift().processorFor(getService());  
    } else {
      throw new IllegalStateException();
    }
  }

  private String strAttr(String attr) {
    return Strings.trimToNull(getRequest().getAttributes().get(attr));
  }

}
