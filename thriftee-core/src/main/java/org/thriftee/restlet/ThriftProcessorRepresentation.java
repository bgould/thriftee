package org.thriftee.restlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftRuntimeException;

public class ThriftProcessorRepresentation extends OutputRepresentation {

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  private final Representation inputEntity;

  private final TProtocolFactory inProtocolFactory;

  private final TProtocolFactory outProtocolFactory;

  private final TProcessor processor;

  public ThriftProcessorRepresentation(
      final Representation inputEntity,
      final TProtocolFactory inProtocolFactory,
      final TProtocolFactory outProtocolFactory,
      final TProcessor processor
    ) {
    super(inputEntity.getMediaType());
    this.inputEntity = inputEntity;
    this.inProtocolFactory = inProtocolFactory;
    this.outProtocolFactory = outProtocolFactory;
    this.processor = processor;
  }

  protected TProtocolFactory getInFactory() {
    return inProtocolFactory;
  }

  protected TProtocolFactory getOutFactory() {
    return outProtocolFactory;
  }

  protected TProcessor getProcessor() {
    return processor;
  }

  @Override
  public void write(final OutputStream out) throws IOException {
    if (out == null) {
      throw new IllegalArgumentException("output stream cannot be null.");
    }
    final InputStream in = inputEntity.getStream();
    final TTransport transport = new TIOStreamTransport(in, out);
    try {
      final TProtocol inProtocol = getInFactory().getProtocol(transport);
      final TProtocol outProtocol = getOutFactory().getProtocol(transport);
      if (getProcessor().process(inProtocol, outProtocol)) {
        transport.flush();
        out.flush();
      } else {
        throw new IOException("TProcessor.process() returned false");
      }
    } catch (TException|RuntimeException e) {
      LOG.error(Messages.PROCESSOR_001.getMessage(), e);
      throw new ThriftRuntimeException(e, Messages.PROCESSOR_001);
    } finally {
      transport.close();
    }
  }

  public static enum Messages implements ThriftMessage {

    PROCESSOR_001("An error processing a thrift service call."),
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
