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

import static net.sf.saxon.s9api.Serializer.Property.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.thriftee.thrift.xml.Transformation.RootType;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public class Transforms {

  private final Processor processor;

  private final ConcurrentMap<String, XsltExecutable> xsltCache;

  private final XsltCompiler compiler;

  public static final String XSL_BASE = "org/thriftee/thrift/xml";

  public static final String XSL_FORMATTER = "pretty-print.xsl";

  public static final String XSL_TO_SIMPLE = "thrift-streaming-to-simple.xsl";

  public static final String XSL_TO_STREAM = "thrift-simple-to-streaming.xsl";

  public static final String XSL_TO_SCHEMA = "thrift-model-to-xsd.xsl";

  public static final String XSL_TO_WSDL   = "thrift-model-to-wsdl.xsl";

  public Transforms() {
    xsltCache = new ConcurrentHashMap<>();
    processor = new Processor(false);
    compiler = processor.newXsltCompiler();
    compiler.setURIResolver(
      new InternalResourceResolver(compiler.getURIResolver())
    );
  }

  public URL schemaUrl() {
    return resolveInternalXsl("thrift-idl.xsd");
  }

  public void preload(File file) throws IOException {
    final Configuration config = processor.getUnderlyingConfiguration();
    try {
      final DocumentInfo docinfo = config.buildDocument(new StreamSource(file));
      final String uri = file.toURI().toURL().toString();
      config.getGlobalDocumentPool().add(docinfo, uri);
    } catch (net.sf.saxon.trans.XPathException e) {
      throw new IOException(e);
    }
  }

  public SimpleToStreamingTransformation newSimpleToStreaming() {
    return new SimpleToStreamingTransformation(this);
  }

  public SimpleToStreamingTransformation newSimpleToStreaming(
      final File modelFile, final String module, final boolean formatted) {
    final SimpleToStreamingTransformation trns = newSimpleToStreaming();
    trns.setModelFile(modelFile);
    trns.setModule(module);
    trns.setFormatting(formatted);
    return trns;
  }

  public void transformSimpleToStreaming(
        final File modelFile, 
        final String module, 
        final Source source, 
        final StreamResult result,
        boolean indent
      ) throws IOException {
    newSimpleToStreaming(modelFile, module, indent).transform(source, result);
  }

  public StreamingToSimpleTransformation newStreamingToSimple() {
    return new StreamingToSimpleTransformation(this);
  }

  public StreamingToSimpleTransformation newStreamingToSimple(
        final File modelFile, final String module, 
        final RootType rootType, final String rootName) {
    final StreamingToSimpleTransformation trns = newStreamingToSimple();
    trns.setModelFile(modelFile);
    trns.setModule(module);
    trns.setRoot(rootType, rootName);
    return trns;
  }

  public void transformStreamingToSimple(
        final File model, final String module, 
        final RootType type, final String name, 
        final Source source, final StreamResult result
      ) throws IOException {
    newStreamingToSimple(model, module, type, name).transform(source, result);
  }

  XsltTransformer newSimpleToStreamingTransformer() {
    return newInternalTransformer(XSL_TO_STREAM);
  }

  XsltTransformer newStreamingToSimpleTransformer() {
    return newInternalTransformer(XSL_TO_SIMPLE);
  }

  XsltTransformer newSchemaToWsdlTransformer() {
    return newInternalTransformer(XSL_TO_WSDL);
  }

  XsltTransformer newSchemaToXsdTransformer() {
    return newInternalTransformer(XSL_TO_SCHEMA);
  }

  protected URL resolveInternalXsl(String s) {
    final ClassLoader cl = getClass().getClassLoader();
    final String rsrc = XSL_BASE + "/" + s;
    final URL url = cl.getResource(rsrc);
    if (url == null) {
      throw new IllegalArgumentException("resource not found: " + rsrc);
    }
    return url;
  }

  protected XsltTransformer newInternalTransformer(String s) {
    try {
      return cachedTransformer(resolveInternalXsl(s));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public XsltTransformer cachedTransformer(URL url) throws IOException {
    try {
      final String key = url.toExternalForm();
      XsltExecutable templates = xsltCache.get(key);
      if (templates == null) {
        try (final InputStream stream = url.openStream()) {
          templates = compiler.compile(new StreamSource(stream));
        }
        final XsltExecutable cached = xsltCache.putIfAbsent(key, templates);
        if (cached != null) {
          templates = cached;
        }
      }
      return templates.load();
    } catch (SaxonApiException e) {
      throw new IOException(e);
    }
  }

  public Serializer serializer(
        final StreamResult result, 
        final boolean formatting
      ) throws IOException {
    if (result == null) {
      throw new IllegalArgumentException("result cannot be null");
    }
    final Serializer out = processor.newSerializer();
    if (result.getWriter() != null) {
      out.setOutputWriter(result.getWriter());
    } else if (result.getOutputStream() != null) {
      out.setOutputStream(result.getOutputStream());
    } else if (result.getSystemId() != null) {
      try {
        out.setOutputFile(new File(new URI(result.getSystemId())));
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    } else {
      throw new IllegalArgumentException(
          "result must have either a writer, output stream, or systemId set");
    }
    out.setOutputProperty(OMIT_XML_DECLARATION, "yes");
    if (formatting) {
      out.setOutputProperty(INDENT, "yes");
      out.setOutputProperty(SAXON_INDENT_SPACES, "2");
    }
    return out;
  }

  public void formatXml(Source source, StreamResult out) throws IOException {
    try {
      final XsltTransformer tr = newInternalTransformer(XSL_FORMATTER);
      tr.setSource(source);
      tr.setDestination(serializer(out, true));
      tr.transform();
    } catch (SaxonApiException e) {
      throw new IOException(e);
    }
  }

  public void formatXml(String xml, StreamResult result) throws IOException {
    formatXml(new StreamSource(new StringReader(xml)), result);
  }

  public void formatXml(File xmlFile, StreamResult result) throws IOException {
    try (final InputStream stream = xmlFile.toURI().toURL().openStream()) {
      formatXml(new StreamSource(stream), result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String formatXml(Source source) throws IOException {
    final StringWriter w = new StringWriter();
    final StreamResult result = new StreamResult(w);
    formatXml(source, result);
    return w.toString();
  }

  public String formatXml(String xml) throws IOException {
    return formatXml(new StreamSource(new StringReader(xml)));
  }

  // TODO: turn this method into a subclass of Transformation
  public Map<String, File> exportWsdls(File model, File tmp) throws IOException {
    final Map<String, File> wsdlFiles = new TreeMap<>();
    final Set<String> modules = moduleNamesFor(model);
    for (final String module : modules) {
      final Set<String> services = serviceNamesFor(module, model);
      final XsltTransformer trans = newSchemaToWsdlTransformer();
      for (String service : services) {
        final String basename = module + "." + service;
        final File wsdlOutput = new File(tmp, basename + ".wsdl");
        trans.setParameter(q("service_module"), strval(module));
        trans.setParameter(q("service_name"), strval(service));
        final StreamSource source = new StreamSource(model);
        final Destination result = processor.newSerializer(wsdlOutput);
        try {
          trans.setSource(source);
          trans.setDestination(result);
          trans.transform();
        } catch (SaxonApiException e) {
          throw new IOException(e);
        } finally {
          trans.clearParameters();
        }
        wsdlFiles.put(basename, wsdlOutput);
      }
    }
    return Collections.unmodifiableMap(wsdlFiles);
  }

  // TODO: turn this method into a subclass of Transformation
  public Map<String, File> exportSchemas(File model, File tmp) throws IOException {
    final Map<String, File> xsdFiles = new TreeMap<>();
    final XsltTransformer trans = newSchemaToXsdTransformer();
    final Set<String> modules = moduleNamesFor(model);
    for (String module : modules) {
      final File schemaOutput = new File(tmp, module + ".xsd");
      final StreamSource source = new StreamSource(model);
      final Destination result = processor.newSerializer(schemaOutput);
      try {
        trans.setParameter(q("root_module"), strval(module));
        trans.setSource(source);
        trans.setDestination(result);
        trans.transform();
      } catch (SaxonApiException e) {
        throw new IOException(e);
      } finally {
        trans.clearParameters();
      }
      xsdFiles.put(module, schemaOutput);
    }
    return Collections.unmodifiableMap(xsdFiles);
  }

  // TODO: use s9api instead of JAXP
  public Set<String> moduleNamesFor(File modelFile) throws IOException {
    final XPathFactory xpathFactory = XPathFactory.newInstance();
    try {
      final String expr = String.format(
        "/*[local-name()='idl']/*[local-name()='document']/@name"
      );
      final XPath xpath = xpathFactory.newXPath();
      final XPathExpression expression = xpath.compile(expr);
      try (FileInputStream in = new FileInputStream(modelFile)) {
        final Set<String> results = new LinkedHashSet<String>();
        final NodeList services = (NodeList) expression.evaluate(
          new InputSource(in), XPathConstants.NODESET
        );
        for (int i = 0, c = services.getLength(); i < c; i++) {
          results.add(services.item(i).getNodeValue());
        }
        return results;
      }
    } catch (XPathException e) {
      throw new IOException(e);
    }
  }

  // TODO: use s9api instead of JAXP
  public Set<String> serviceNamesFor(String module, File modelFile)
      throws IOException {
    try {
      final XPathFactory xpathFactory = XPathFactory.newInstance();
      final String expr = String.format(
        "/*[local-name()='idl']" + 
        "/*[local-name()='document' and @name='%s']" + 
        "/*[local-name()='service']/@name", 
        module
      );
      final XPath xpath = xpathFactory.newXPath();
      final XPathExpression expression = xpath.compile(expr);
      try (FileInputStream in = new FileInputStream(modelFile)) {
        final Set<String> results = new LinkedHashSet<String>();
        final NodeList services = (NodeList) expression.evaluate(
          new InputSource(in), XPathConstants.NODESET
        );
        for (int i = 0, c = services.getLength(); i < c; i++) {
          results.add(services.item(i).getNodeValue());
        }
        return results;
      }
    } catch (XPathException e) {
      throw new IOException(e);
    }
  }

  private static final QName q(String localName) {
    return new QName(localName);
  }

  private static final XdmValue strval(String s) {
    return new XdmAtomicValue(s);
  }

  public static class InternalResourceResolver implements URIResolver {

    private final URIResolver delegate;

    private final Pattern resolverPattern = Pattern.compile("^thrift-.+xsl$");

    public InternalResourceResolver(URIResolver delegate) {
      super();
      this.delegate = delegate;
    }

    public Source resolve(String href, String b) throws TransformerException {
      try {
        final URL url;
        final ClassLoader cl = getClass().getClassLoader();
        if (href.startsWith("classpath:")) {
          final String rsrc = href.substring(10);
          url = cl.getResource(rsrc);
        } else {
          final Matcher m = resolverPattern.matcher(href);
          if (m.matches()) {
            
            final String rsrc = XSL_BASE + "/" + href;
            url = cl.getResource(rsrc);
          } else {
            url = null;
          }
        }
        if (url != null) {
          return new StreamSource(url.openStream());
        }
      } catch (IOException e) {
        throw new TransformerException(e);
      }
      return delegate.resolve(href, b);
    }

  }

}
