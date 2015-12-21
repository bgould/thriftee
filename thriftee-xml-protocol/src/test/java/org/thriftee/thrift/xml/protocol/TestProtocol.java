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
package org.thriftee.thrift.xml.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.transport.TIOStreamTransport;
import org.thriftee.thrift.xml.protocol.TXMLProtocol;

public class TestProtocol extends TXMLProtocol {

  public TestProtocol(String in, Variant variant) {
    this(in == null ? null : in.getBytes(), variant);
  }

  public TestProtocol(byte[] input, Variant variant) {
    super(new TestTransport(input), variant, false);
  }

  @Override
  public TestTransport getTransport() {
    return (TestTransport) super.getTransport();
  }
  
  public ByteArrayInputStream getIn() {
    return getTransport().getIn();
  }

  public ByteArrayOutputStream getOut() {
    return getTransport().getOut();
  }

  public String getStringOutput() {
    return new String(getTransport().getOut().toByteArray());
  }

  public StreamSource asSource() {
    return new StreamSource(new StringReader(getStringOutput()));
  }

  public void writeOutputTo(File file) throws IOException {
    try (final OutputStream w = new FileOutputStream(file)) {
      w.write(getOut().toByteArray());
      w.flush();
    }
  }

  public String getFormattedXmlOutput() {
    return TXMLProtocol.XML.formatXml(getStringOutput());
  }

  public static class TestTransport extends TIOStreamTransport {

    public TestTransport(byte[] input) {
      super(
        input == null ? null : new ByteArrayInputStream(input), 
        new ByteArrayOutputStream()
      );
    }

    public ByteArrayInputStream getIn() {
      return (ByteArrayInputStream) inputStream_;
    }

    public ByteArrayOutputStream getOut() {
      return (ByteArrayOutputStream) outputStream_;
    }

  }

}
