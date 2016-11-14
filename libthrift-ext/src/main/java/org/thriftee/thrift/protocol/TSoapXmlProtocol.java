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
package org.thriftee.thrift.protocol;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.thriftee.thrift.protocol.xml.SimpleToStreamingTransformation;
import org.thriftee.thrift.protocol.xml.StreamingToSimpleTransformation;
import org.thriftee.thrift.protocol.xml.Transformation.RootType;
import org.thriftee.thrift.protocol.xml.Transforms;
import org.thriftee.thrift.transport.TTransportInputStream;
import org.thriftee.thrift.transport.TTransportOutputStream;

import net.sf.saxon.s9api.SaxonApiException;

public class TSoapXmlProtocol extends TProtocolDecorator {

  public static class Factory implements TProtocolFactory {

    private static final long serialVersionUID = 1017378360734059748L;

    public Factory() {
    }

    public Factory(Transforms transforms) {
      super();
      this.transforms = transforms;
    }

    @Override
    public TSoapXmlProtocol getProtocol(TTransport transport) {
      final Transforms transforms = getTransforms();
      if (transforms == null) {
        throw new IllegalStateException("Transforms object is null.");
      }
      if (modelFile == null) {
        throw new IllegalStateException("modelFile is null.");
      }
      return new TSoapXmlProtocol(
        transport,
        transforms,
        modelFile,
        getModuleName(),
        getServiceName(),
        getStructName()
      );
    }

    private Transforms transforms;

    private File modelFile;

    private String moduleName;

    private String serviceName;

    private String structName;

    public Transforms getTransforms() {
      return transforms;
    }

    public void setTransforms(Transforms transforms) {
      this.transforms = transforms;
    }

    public String getModuleName() {
      return moduleName;
    }

    public void setModuleName(String moduleName) {
      this.moduleName = moduleName;
    }

    public String getServiceName() {
      return serviceName;
    }

    public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
    }

    public String getStructName() {
      return structName;
    }

    public void setStructName(String structName) {
      this.structName = structName;
    }

    public File getModelFile() {
      return modelFile;
    }

    public void setModelFile(File modelFile) {
      this.modelFile = modelFile;
    }

  }

  private final Transforms transforms;

  private final File modelFile;

  private String moduleName;

  private String serviceName;

  private String structName;

  private int structCounter = -1;

  public TSoapXmlProtocol(
        final TTransport transport,
        final Transforms transforms,
        final File modelFile,
        final String moduleName,
        final String serviceName,
        final String structName
      ) {
    super(transport);
    this.transforms = transforms;
    this.modelFile = modelFile;
    this.moduleName = moduleName;
    this.serviceName = serviceName;
    this.structName = structName;
  }

  @Override
  public void writeMessageBegin(TMessage message) throws TException {
    if (concreteProtocol_ == null) {
      concreteProtocol_ = new TXMLProtocol(new TMemoryBuffer(4096));
    }
    super.writeMessageBegin(message);
  }

  @Override
  public void writeStructBegin(TStruct struct) throws TException {
    if (concreteProtocol_ == null) {
      concreteProtocol_ = new TXMLProtocol(new TMemoryBuffer(4096));
      structCounter = 0;
    } else if (structCounter > -1) {
      structCounter++;
    }
    super.writeStructBegin(struct);
  }

  @Override
  public void writeMessageEnd() throws TException {
    super.writeMessageEnd();
    writeTransformedOutput(RootType.MESSAGE, getServiceName());
  }

  @Override
  public void writeStructEnd() throws TException {
    super.writeStructEnd();
    if (structCounter > -1) {
      structCounter--;
      if (structCounter < 0) {
        writeTransformedOutput(RootType.STRUCT, getStructName());
        structCounter = -1;
      }
    }
  }

  @Override
  public TMessage readMessageBegin() throws TException {
    if (concreteProtocol_ == null) {
      concreteProtocol_ = makeConcreteProtocol(RootType.MESSAGE, getServiceName());
    }
    return super.readMessageBegin();
  }

  @Override
  public TStruct readStructBegin() throws TException {
    if (concreteProtocol_ == null) {
      concreteProtocol_ = makeConcreteProtocol(RootType.STRUCT, getStructName());
    }
    return super.readStructBegin();
  }

  protected TXMLProtocol makeConcreteProtocol(
      final RootType rootType, final String rootName) throws TException {
    final InputStream in = new TTransportInputStream(getTransport());
    final TByteArrayOutputStream out = new TByteArrayOutputStream(4096);
    final StreamSource source = new StreamSource(in);
    final StreamResult result = new StreamResult(out);
    final SimpleToStreamingTransformation trns;
    final TTransport transport;
    trns = transforms.newSimpleToStreaming();
    trns.setFormatting(false);
    trns.setModelFile(modelFile);
    trns.setModule(getModuleName());
    trns.setRoot(rootType, rootName);
    try {
      trns.transform(source, result);
      transport = new TMemoryInputTransport(out.get(), 0, out.len());
      return new TXMLProtocol(transport);
    } catch (IOException e) {
      if (e.getCause() != null && e.getCause() instanceof SaxonApiException) {
        throw new TProtocolException(e.getCause());
      } else {
        throw new TTransportException(e);
      }
    }
  }

  protected void writeTransformedOutput(
      final RootType rootType, final String rootName) throws TException {
    final TMemoryBuffer buffer = ((TMemoryBuffer) concreteProtocol_.getTransport());
    final InputStream in = new ByteArrayInputStream(buffer.getArray(), 0, buffer.length());
    final OutputStream out = new TTransportOutputStream(getTransport());
    final StreamSource source = new StreamSource(in);
    final StreamResult result = new StreamResult(out);
    final StreamingToSimpleTransformation trns;
    trns = transforms.newStreamingToSimple();
    trns.setFormatting(false);
    trns.setModelFile(modelFile);
    trns.setModule(getModuleName());
    trns.setRoot(rootType, rootName);
    try {
      trns.transform(source, result);
    } catch (IOException e) {
      if (e.getCause() != null && e.getCause() instanceof SaxonApiException) {
        throw new TProtocolException(e.getCause());
      } else {
        throw new TTransportException(e);
      }
    }
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getStructName() {
    return structName;
  }

  public void setStructName(String structName) {
    this.structName = structName;
  }

}