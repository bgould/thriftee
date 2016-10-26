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
import java.io.OutputStream;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.schema.MethodSchema;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;

public class RestResource extends AbstractProcessorResource {

  private String filename;

  @Override
  boolean resolveRemaining() {
    this.filename = strAttr("filename");
    if (this.filename != null) {
      return this.filename.equals("swagger.json")
//          || getSchemaFilenames().contains(this.filename)
             ;
    }
    return true; // no filename specified
  }

  @Override
  public Representation getRepresentation() {
    if (getFilename() != null) {
      return showFile();
    }
    if (getService() != null) {
      return listFiles();
    }
    return null;
  }

  @Override
  public AbstractProcessorRepresentation processorFor(Representation entity) {
    return new RestProcessor(entity, getMethodSchema(), getProcessor());
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
    directory.getFiles().put("swagger.json", "swagger.json");
//    for (final String xsdfile : getSchemaFilenames()) {
//      directory.getFiles().put(xsdfile, xsdfile);
//    }
    return listing(directory);
  }

  protected Representation showFile() {
    if (getFilename() == null) {
      throw new IllegalStateException("getFilename() should not be null");
    }
//    final String filename;
    if ("swagger.json".equals(getFilename())) {
      return new SwaggerRepresentation();
    } else {
      throw new IllegalStateException();
    }
//    else {
//      filename = getFilename();
//      return new FileRepresentation(fileFor(filename), MediaType.TEXT_XML);
//    }
  }

//  private final File fileFor(String filename) {
//    return new File(thrift().wsdlClientDir(), filename);
//  }
//
//  private String getWsdlFilename() {
//    return getModule().getName() + "." + getService().getName() + ".wsdl";
//  }
//
//  private SortedSet<String> getSchemaFilenames() {
//    if (_schemaFilenames == null) {
//      if (getModule() == null) {
//        throw new IllegalStateException("getModule() should not be null");
//      }
//      final TreeSet<String> result = new TreeSet<>();
//      result.add(getModule().getName() + ".xsd");
//      for (String include : getModule().getIncludes()) {
//        if (include.endsWith(".thrift")) {
//          include = include.substring(0, include.length() - 7);
//        }
//        result.add(include + ".xsd");
//      }
//      this._schemaFilenames = result;
//    }
//    return _schemaFilenames;
//  }

  private String getFilename() {
    return this.filename;
  }

  private MethodSchema getMethodSchema() {
    throw new UnsupportedOperationException();
  }

  public static class RestProcessor extends AbstractProcessorRepresentation {

    private static final TProtocolFactory fctry = new TBinaryProtocol.Factory();

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Representation entity;

    private final MethodSchema methodSchema;

    public RestProcessor(
          final Representation inputEntity,
          final MethodSchema methodSchema,
          final TProcessor processor
        ) {
      super(MediaType.APPLICATION_JSON, fctry, fctry, processor);
      this.entity = inputEntity;
      this.methodSchema = methodSchema;
    }

    @Override
    public void write(final OutputStream out) throws IOException {

      throw new UnsupportedOperationException();
      // transform XML input to TBinaryProtocol
//      final TMemoryBuffer buf = new TMemoryBuffer(4096);
//      try {
//        final TProtocol inProtocol = fctry.getProtocol(buf);
//        transforms.fromJson(methodSchema, jsonIn, inProtocol);
//      } catch (TException|IOException e) {
//        throw new IOException("error transforming to streaming protocol", e);
//      }

      // run TProcessor
//      final TByteArrayOutputStream baos = new TByteArrayOutputStream(2048);
//      process(new ByteArrayInputStream(buf.getArray(), 0, buf.length()), baos);
//
      // transform TXMLProtocol to XML response
//      final TTransport t = new TMemoryInputTransport(baos.get(), 0, baos.len());
//      final TProtocol protocol = fctry.getProtocol(t);
//      try {
//        transforms.toJson(methodSchema, protocol, jsonOut, false);
//      } catch (TException|IOException e) {
//        throw new IOException("error transforming to streaming protocol", e);
//      }

    }

  }

  public static class SwaggerRepresentation extends OutputRepresentation {

    public SwaggerRepresentation(
//        final ThriftEE thriftee,
//        final File wsdlFile,
//        final String soapAddress
      ) {
      super(MediaType.APPLICATION_JSON); //, thriftee, templateUrl());
//      this.wsdlFile = wsdlFile;
//      this.soapAddress = soapAddress;
    }

    @Override
    public void write(final OutputStream out) throws IOException {
      throw new UnsupportedOperationException();
    }

  }

}
