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
package org.thriftee.restlet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.thriftee.framework.ThriftEE;
import org.thriftee.thrift.xml.Transforms;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltTransformer;

public abstract class TransformerRepresentation extends OutputRepresentation {

  private static final DocumentBuilderFactory dbf = 
                                  DocumentBuilderFactory.newInstance();

  protected Document newDocument() {
    return documentBuilder.newDocument();
  }

  private final ThriftEE thriftee;

  private final DocumentBuilder documentBuilder;

  private final URL url;

  protected TransformerRepresentation(
      final MediaType mediaType, 
      final ThriftEE thriftee,
      final URL url) {
    super(mediaType);
    if (thriftee == null) {
      throw new IllegalArgumentException("ThriftEE instance cannot be null.");
    }
    try {
      this.documentBuilder = dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    this.thriftee = thriftee;
    this.url = url;
  }

  protected final ThriftEE thriftee() {
    return thriftee;
  }

  @Override
  public void write(OutputStream outputStream) throws IOException {
    final StreamResult result = new StreamResult(outputStream);
    final Serializer serializer = transforms().serializer(result, false);
    final XsltTransformer transformer = transformer();
    configure(transformer);
    transformer.setDestination(serializer);
    try {
      transformer.setSource(source());
      transformer.transform();
    } catch (final SaxonApiException e) {
      throw new IOException(e);
    }
  }

  private Transforms transforms() {
    return thriftee.xmlTransforms();
  }

  protected XsltTransformer transformer() throws IOException {
    return thriftee.xmlTransforms().cachedTransformer(url);
  }

  protected void configure(XsltTransformer transformer) {}

  protected abstract Source source() throws IOException;

}
