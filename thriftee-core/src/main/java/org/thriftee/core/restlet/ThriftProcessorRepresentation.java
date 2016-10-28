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

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.restlet.representation.Representation;

public class ThriftProcessorRepresentation
                extends AbstractProcessorRepresentation {

  private final TProtocolFactory inProtocolFactory;

  private final TProtocolFactory outProtocolFactory;

  private final Representation input;

  private OutputStream outputStream;

  public ThriftProcessorRepresentation(
      final Representation input,
      final TProtocolFactory inProtocolFactory,
      final TProtocolFactory outProtocolFactory,
      final TProcessor processor
    ) {
    super(input.getMediaType(), processor);
    this.inProtocolFactory = inProtocolFactory;
    this.outProtocolFactory = outProtocolFactory;
    this.input = input;
  }

  @Override
  protected TProtocol getInProtocol() throws IOException {
    final TTransport transport = new TIOStreamTransport(input.getStream());
    return inProtocolFactory.getProtocol(transport);
  }

  @Override
  protected TProtocol getOutProtocol() throws IOException {
    final TTransport transport = new TIOStreamTransport(getOutputStream());
    return outProtocolFactory.getProtocol(transport);
  }

  protected OutputStream getOutputStream() {
    return this.outputStream;
  }

  @Override
  public void write(final OutputStream out) throws IOException {
    this.outputStream = out;
    try {
      process();
    } catch (TException e) {
      throw new IOException(e);
    } finally {
      this.outputStream = null;
    }
  }

}
