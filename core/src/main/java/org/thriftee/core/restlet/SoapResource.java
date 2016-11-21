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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.core.ThriftEE;
import org.thriftee.thrift.protocol.TSOAPProtocol;
import org.thriftee.thrift.protocol.TSoapXmlProtocol;
import org.thriftee.thrift.protocol.TXMLProtocol;
import org.thriftee.thrift.protocol.xml.Transformation.RootType;
import org.thriftee.thrift.protocol.xml.Transforms;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltTransformer;

public class SoapResource extends AbstractProcessorResource {

  private SortedSet<String> _schemaFilenames;

  private String filename;

  @Override
  boolean resolveRemaining() {
    this.filename = strAttr("filename");
    if (this.filename != null) {
      return this.filename.equals("service.wsdl") ||
             getSchemaFilenames().contains(this.filename);
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
    String q = getQuery().getValues("new");
    if (q != null) {
      final TSOAPProtocol.Factory fct = new TSOAPProtocol.Factory(getService());
      return new ThriftProcessorRepresentation(entity, fct, fct, getProcessor());
    } else {
      final TSoapXmlProtocol.Factory fct = new TSoapXmlProtocol.Factory(
        thrift().xmlTransforms()
      );
      fct.setModelFile(thrift().globalXmlFile());
      fct.setModuleName(getModule().getName());
      fct.setServiceName(getService().getName());
      return new ThriftProcessorRepresentation(entity, fct, fct, getProcessor());
      /*
      return new Processor(
        entity,
        thrift().xmlTransforms(),
        thrift().globalXmlFile(),
        getModule().getName(),
        getService().getName(),
        getProcessor()
      );
      */
    }
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
      final TreeSet<String> result = new TreeSet<>();
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

  public static class Processor extends AbstractProcessorRepresentation {

    private static final TXMLProtocol.Factory fctry = new TXMLProtocol.Factory();

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private final Representation inputEntity;

    private final Transforms transforms;

    private final File modelFile;

    private final String moduleName;

    private final String serviceName;

    private TMemoryBuffer outputBuffer;

    public Processor(
        final Representation inputEntity,
        final Transforms transforms,
        final File modelFile,
        final String moduleName,
        final String serviceName,
        final TProcessor processor
      ) {
      super(MediaType.TEXT_XML, processor);
      this.inputEntity = inputEntity;
      this.transforms = transforms;
      this.modelFile = modelFile;
      this.moduleName = moduleName;
      this.serviceName = serviceName;
    }

    @Override
    public void write(final OutputStream out) throws IOException {
      try {
        process();
      } catch (TException e) {
        throw new IOException(e);
      }
      // transform TXMLProtocol to XML response
      final byte[] arr = outputBuffer.getArray();
      final int len = outputBuffer.length();
      final InputStream outStream = new ByteArrayInputStream(arr, 0, len);
      final StreamSource outSource = new StreamSource(outStream);
      final StreamResult outResult = new StreamResult(out);
      try {
        transforms.transformStreamingToSimple(
          modelFile,
          moduleName,
          RootType.MESSAGE,
          serviceName,
          outSource,
          outResult
        );
      } catch (IOException e) {
        throw new IOException("error transforming to streaming protocol", e);
      }
    }

    @Override
    protected TProtocol getInProtocol() throws IOException {
      // transform XML input to TXMLProtocol
      final TByteArrayOutputStream inBytes = new TByteArrayOutputStream(2048);
      final StreamSource inSource = new StreamSource(inputEntity.getStream());
      final StreamResult inResult = new StreamResult(inBytes);
      transforms.transformSimpleToStreaming(
        modelFile, moduleName, inSource, inResult, false
      );
      final TTransport transport = new TMemoryInputTransport(
        inBytes.get(), 0, inBytes.len()
      );
      return fctry.getProtocol(transport);
    }

    @Override
    protected TProtocol getOutProtocol() throws IOException {
      this.outputBuffer = new TMemoryBuffer(4096);
      return fctry.getProtocol(this.outputBuffer);
    }

  }

  public static class WsdlRepresentation extends TransformerRepresentation {

    private final File wsdlFile;

    private final String soapAddress;

    public WsdlRepresentation(
        final ThriftEE thriftee, final File wsdlFile, final String soapAddress
      ) {
      super(MediaType.TEXT_XML, thriftee, templateUrl());
      this.wsdlFile = wsdlFile;
      this.soapAddress = soapAddress;
    }

    private static final URL templateUrl() {
      return DirectoryListingRepresentation.class.getClassLoader().getResource(
        FrameworkResource.XSLT_PREFIX + "wsdl_location.xsl"
      );
    }

    @Override
    protected final void configure(XsltTransformer transformer) {
      final QName param = new QName("wsdl_location");
      transformer.setParameter(param, new XdmAtomicValue(soapAddress));
    }

    @Override
    protected final Source source() throws IOException {
      return new StreamSource(getWsdlFile());
    }

    protected final File getWsdlFile() throws IOException {
      final File file = wsdlFile;
      if (!file.exists()) {
        throw new FileNotFoundException(file.getAbsolutePath());
      }
      if (!file.isFile()) {
        throw new IOException("not a file: " + file.getAbsolutePath());
      }
      return file;
    }

  }

}
