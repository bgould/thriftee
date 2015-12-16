package org.thriftee.restlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TProcessor;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.thrift.xml.Transforms;
import org.thriftee.thrift.xml.protocol.TXMLProtocol;
import org.thriftee.thrift.xml.protocol.TXMLProtocol.Variant;

public class SOAPProcessorRepresentation extends OutputRepresentation {

  private static final TXMLProtocol.Factory fctry = new TXMLProtocol.Factory(
    Variant.VERBOSE, 
    false
  );

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final Representation inputEntity;

  private final Transforms transforms;

  private final TProcessor processor;

  private final File modelFile;

  private final String moduleName;

  private final String serviceName;

  public SOAPProcessorRepresentation(
      final Representation inputEntity,
      final File modelFile,
      final String moduleName,
      final String serviceName,
      final Transforms transforms,
      final TProcessor processor
    ) {
    super(inputEntity.getMediaType());
    this.inputEntity = inputEntity;
    this.transforms = transforms;
    this.processor = processor;
    this.modelFile = modelFile;
    this.moduleName = moduleName;
    this.serviceName = serviceName;
  }

  protected TProcessor getProcessor() {
    return processor;
  }

  @Override
  public void write(final OutputStream out) throws IOException {

    final StringWriter inString = new StringWriter();
    final StreamSource inSource = new StreamSource(inputEntity.getStream());
    final StreamResult inResult = new StreamResult(inString);
    try {
      transforms.transformSimpleToStreaming(
        modelFile,
        moduleName,
        inSource,
        inResult
      );
    } catch (TransformerException e) {
      LOG.error("error transforming to streaming protocol", e);
      throw new IOException(e);
    }

    final StringBuffer inBuffer = inString.getBuffer();
    final TByteArrayOutputStream baos = new TByteArrayOutputStream();
    createDelegate(inBuffer).write(baos);
    final byte[] response = baos.get();
    final int len = baos.len();

    final InputStream outStream = new ByteArrayInputStream(response, 0, len);
    final Source outSource = new StreamSource(outStream);
    final Result outResult = new StreamResult(out);
    try {
      transforms.transformStreamingToSimple(
        modelFile,
        moduleName,
        serviceName,
        outSource,
        outResult
      );
    } catch (TransformerException e) {
      LOG.error("error transforming to simple protocol", e);
      throw new IOException(e);
    }

  }

  private ThriftProcessorRepresentation createDelegate(CharSequence seq) {
    final Representation input = new StringRepresentation(seq);
    input.setMediaType(MediaType.TEXT_XML);
    return new ThriftProcessorRepresentation(input, fctry, fctry, processor);
  }

  public static enum Messages implements ThriftMessage {

    SOAP_PROCESSOR_001("An error processing a SOAP service call."),
    ;

    private final String msg;

    private Messages(String msg) {
      this.msg = msg;
    }

    public String getCode() {
      return name();
    }

    public String getMessage() {
      return msg;
    }

  }

}
