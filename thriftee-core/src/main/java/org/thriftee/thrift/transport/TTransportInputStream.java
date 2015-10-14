package org.thriftee.thrift.transport;

import java.io.IOException;
import java.io.InputStream;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class TTransportInputStream extends InputStream {

  private final TTransport __transport;

  private final byte[] byteRawBuf = new byte[1];

  public TTransportInputStream(TTransport transport) {
    this.__transport = transport;
  }

  protected TTransport transport() {
    return __transport;
  }

  @Override
  public int read() throws IOException {
    final TTransport trans = transport();
    byte b;
    if (trans.getBytesRemainingInBuffer() > 0) {
      b = trans.getBuffer()[trans.getBufferPosition()];
      trans.consumeBuffer(1);
    } else {
      try {
        trans.readAll(byteRawBuf, 0, 1);
      } catch (TTransportException e) {
        throw wrap(e); 
      }
      b = byteRawBuf[0];
    }
    return b & 0xff;
  }

  @Override
  public int read(byte[] b) throws IOException {
    try {
      return transport().read(b, 0, b.length);
    } catch (TTransportException e) {
      throw wrap(e);
    }
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    try {
      return transport().read(b, off, len);
    } catch (TTransportException e) {
      throw wrap(e);
    }
  }

  public IOException wrap(TTransportException e) throws IOException {
    e.printStackTrace();
    return new IOException(
      "Error reading from thrift transport " + transport() + 
      ": " + e.getMessage(), e
    );
  }

}
