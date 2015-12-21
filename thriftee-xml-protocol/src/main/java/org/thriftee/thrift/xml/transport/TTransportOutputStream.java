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
package org.thriftee.thrift.xml.transport;

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
    throw new IOException(
      "Error reading from thrift transport " + transport() + 
      ": " + e.getMessage(), e
    );
  }

}
