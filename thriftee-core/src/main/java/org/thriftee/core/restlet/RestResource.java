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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.thrift.schema.MethodSchema;
import org.thriftee.thrift.schema.ModuleSchema;
import org.thriftee.thrift.schema.SchemaException;
import org.thriftee.thrift.schema.ServiceSchema;
import org.thriftee.thrift.xml.protocol.TJsonApiProtocol;

public class RestResource extends AbstractProcessorResource {

  private String filename;

  @Override
  boolean resolveRemaining() {
    this.filename = strAttr("filename");
    if (this.filename != null) {
      return this.filename.equals("swagger.json") ||
              getService().getMethods().containsKey(filename);
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
  public AbstractProcessorRepresentation processorFor(Representation entity)
      throws IOException {
    try {
      return new RestProcessor(entity, getMethodSchema(), getProcessor());
    } catch (SchemaException e) {
      getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
      return null;
    }
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
    final Set<String> set = new TreeSet<>(getService().getMethods().keySet());
    for (final String methodname : set) {
      directory.getFiles().put(methodname, methodname);
    }
    return listing(directory);
  }

  protected Representation showFile() {
    if (getFilename() == null) {
      System.err.println("getFilename() should not be null");
      throw new IllegalStateException("getFilename() should not be null");
    }
//    final String filename;
    if ("swagger.json".equals(getFilename())) {
      return new SwaggerRepresentation();
    } else {
      final DirectoryListingModel directory = createDefaultModel(false);
      return listing(directory);
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

  private MethodSchema getMethodSchema() throws SchemaException {
    return getService().findMethod(getFilename());
  }

  protected class RestProcessor extends AbstractProcessorRepresentation {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Representation entity;

    private final MethodSchema methodSchema;

    private OutputStream outputStream;

    protected RestProcessor(
          final Representation inputEntity,
          final MethodSchema methodSchema,
          final TProcessor processor) throws IOException {
      super(MediaType.APPLICATION_JSON, processor);
      this.entity = inputEntity;
      this.methodSchema = methodSchema;
    }

    @Override
    protected TProtocol getInProtocol() throws IOException {
      final TTransport transport = new TIOStreamTransport(entity.getStream());
      return new RestSimpleJsonProtocol(transport);
    }

    @Override
    protected TProtocol getOutProtocol() {
      final OutputStream out = requireNonNull(this.outputStream);
      final TTransport transport = new TIOStreamTransport(out);
      return new RestSimpleJsonProtocol(transport);
    }

    protected class RestSimpleJsonProtocol extends TJsonApiProtocol {

      protected RestSimpleJsonProtocol(TTransport trans) {
        super(trans, null, null);
      }

      @Override
      public void writeMessageBegin(TMessage msg) throws TException {
        switch (msg.type) {
        case TMessageType.CALL:
          throw ex("CALL messages not supported.");
        case TMessageType.REPLY:
          setBaseStruct(methodSchema.getResultStruct());
          break;
        case TMessageType.EXCEPTION:
          getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
          setBaseStruct(methodSchema.getRoot().applicationExceptionSchema());
          break;
        case TMessageType.ONEWAY:
          throw ex("ONEWAY messages not supported");
        default:
          throw ex("invalid message type: " + msg.type);
        }
      }

      @Override
      public void writeMessageEnd() throws TException {}

      @Override
      public TMessage readMessageBegin() throws TException {
        setBaseStruct(methodSchema.getArgumentStruct());
        return new TMessage(methodSchema.getName(), TMessageType.CALL, 1);
      }

      @Override
      public void readMessageEnd() throws TException {}

    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
      this.outputStream = outputStream;
      try {
        process();
      } catch (TException e) {
        throw new IOException(e);
      } finally {
        this.outputStream = null;
      }
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
