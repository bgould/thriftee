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
    if (!trans.isOpen() || !trans.peek()) {
      return -1;
    }
    byte b;
    if (trans.getBytesRemainingInBuffer() > 0) {
      b = trans.getBuffer()[trans.getBufferPosition()];
      trans.consumeBuffer(1);
    } else {
      try {
        trans.readAll(byteRawBuf, 0, 1);
      } catch (TTransportException e) {
        if (e.getType() == TTransportException.END_OF_FILE) {
          return -1;
        }
        throw wrap(e);
      }
      b = byteRawBuf[0];
    }
    return b & 0xff;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    final TTransport trans = transport();
    if (!trans.isOpen() || !trans.peek()) {
      return -1;
    }
    final int buffered = trans.getBytesRemainingInBuffer();
    if (buffered == 0) {
      return -1;
    } else if (buffered > 0) {
      final int pos = trans.getBufferPosition();
      final int amt = Math.min(buffered, len);
      System.arraycopy(trans.getBuffer(), pos, b, off, amt);
      trans.consumeBuffer(amt);
      return amt;
    } else {
      try {
        return trans.read(b, off, len);
      } catch (TTransportException e) {
        if (e.getType() == TTransportException.END_OF_FILE) {
          return -1;
        }
        throw wrap(e);
      }
    }
  }

  public IOException wrap(TTransportException e) throws IOException {
    return new IOException(
      "Error reading from thrift transport " + transport() +
      ": " + e.getMessage(), e
    );
  }

}