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
package org.thriftee.thrift.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

public abstract class Transformation {

  public static enum RootType {
    MESSAGE,
    STRUCT
  }

  protected final Transforms transforms;

  protected File modelFile;

  protected String module;

  protected RootType rootType;

  protected String rootName;

  private boolean formatting;

  protected Transformation(Transforms transforms) {
    this.transforms = transforms;
  }

  public void transform(
        final Source source, 
        final StreamResult result
      ) throws IOException {
    final XsltTransformer transformer = newTransformer();
    final Serializer serializer = transforms.serializer(result, formatting);
    transformer.setDestination(serializer);
    try {
      transformer.setSource(source);
      transformer.transform();
    } catch (SaxonApiException e) {
      throw new IOException(e);
    }
  }

  public final void setRoot(final RootType rootType, final String rootName) {
    if (rootType == null) {
      throw new IllegalArgumentException("rootType cannot be null.");
    }
    if (rootName == null) {
      throw new IllegalArgumentException("rootName cannot be null.");
    }
    this.rootType = rootType;
    this.rootName = rootName;
  }

  public final RootType getRootType() {
    return this.rootType;
  }

  public final String getRootName() {
    return this.rootName;
  }

  public String getModule() {
    return module;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public File getModelFile() {
    return modelFile;
  }

  public void setModelFile(File modelFile) {
    this.modelFile = modelFile;
  }

  public final boolean isFormatting() {
    return formatting;
  }

  public final void setFormatting(boolean formatting) {
    this.formatting = formatting;
  }

  protected abstract XsltTransformer newTransformer() throws IOException;

  protected static final QName q(String localName) {
    return new QName(localName);
  }

  protected static final XdmValue urlval(File file) throws IOException {
    return new XdmAtomicValue(file.toURI().toURL().toString());
  }

  protected static final XdmValue strval(String s) {
    return new XdmAtomicValue(s);
  }

}
