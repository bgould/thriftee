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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.restlet.data.MediaType;
import org.thriftee.core.ThriftEE;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltTransformer;

public class WsdlRepresentation extends TransformerRepresentation {

  private final File wsdlFile;

  private final String soapAddress;
  
  public WsdlRepresentation(
      final ThriftEE thriftee, final File wsdlFile, final String soapAddress
    ) {
    super(MediaType.TEXT_XML, thriftee, templateUrl());
    this.wsdlFile = wsdlFile;
    this.soapAddress = soapAddress;
  }

  private static final URL templateUrl() {
    return DirectoryListingRepresentation.class.getClassLoader().getResource(
      FrameworkResource.XSLT_PREFIX + "wsdl_location.xsl"
    );
  }

  @Override
  protected final void configure(XsltTransformer transformer) {
    final QName param = new QName("wsdl_location");
    transformer.setParameter(param, new XdmAtomicValue(soapAddress));
  }

  @Override
  protected final Source source() throws IOException {
    return new StreamSource(getWsdlFile());
  }

  protected final File getWsdlFile() throws IOException {
    final File file = wsdlFile;
    if (!file.exists()) {
      throw new FileNotFoundException(file.getAbsolutePath());
    }
    if (!file.isFile()) {
      throw new IOException("not a file: " + file.getAbsolutePath());
    }
    return file;
  }

}
