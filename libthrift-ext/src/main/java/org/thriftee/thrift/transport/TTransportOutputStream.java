package org.thriftee.thrift.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class TTransportOutputStream extends OutputStream {

  private final TTransport __transport;

  public TTransportOutputStream(TTransport transport) {
    this.__transport = transport;
  }

  protected TTransport transport() {
    return __transport;
  }

  private final byte[] byteRawBuf = new byte[1];

  @Override
  public void write(int b) throws IOException {
    byteRawBuf[0] = (byte)(b & 0xff);
    try {
      transport().write(byteRawBuf);
    } catch (TTransportException e) {
      throw wrap(e);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    try {
      transport().write(b, off, len);
    } catch (TTransportException e) {
      throw wrap(e);
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    try {
      transport().write(b);
    } catch (TTransportException e) {
      throw wrap(e);
    }
  }

  public IOException wrap(TTransportException e) throws IOException {
    for (Throwable t = e.getCause(); t != null; t = t.getCause()) {
      if (t instanceof IOException) {
        throw (IOException) t;
      }
    }
    throw new IOException(
      "Error writing to thrift transport " + transport() +
      ": " + e.getMessage(), e
    );
  }

}