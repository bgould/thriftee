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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.restlet.data.MediaType;

public class ThriftProcessorRepresentation
                extends AbstractProcessorRepresentation {

  private final InputStream inputStream;

  public ThriftProcessorRepresentation(
      final MediaType mediaType,
      final InputStream inputStream,
      final TProtocolFactory inProtocolFactory,
      final TProtocolFactory outProtocolFactory,
      final TProcessor processor
    ) {
    super(mediaType, inProtocolFactory, outProtocolFactory, processor);
    this.inputStream = inputStream;
  }

  protected InputStream getInputStream() {
    return this.inputStream;
  }

  @Override
  public void write(final OutputStream out) throws IOException {
    process(getInputStream(), out);
  }

}
