package org.thriftee.thrift.xml;

import static net.sf.saxon.s9api.Serializer.Property.INDENT;
import static net.sf.saxon.s9api.Serializer.Property.OMIT_XML_DECLARATION;
import static net.sf.saxon.s9api.Serializer.Property.SAXON_INDENT_SPACES;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

  private final ConcurrentMap<URL, XsltExecutable> xsltCache;

  private final XsltCompiler compiler;

  public static final String XSL_BASE = "org/thriftee/thrift/xml/protocol";

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

  XsltTransformer newSimpleToStreamingTransformer() {
    return newInternalTransformer(XSL_TO_STREAM);
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

  XsltTransformer newStreamingToSimpleTransformer() {
    return newInternalTransformer(XSL_TO_SIMPLE);
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

  public XsltTransformer newSchemaToWsdlTransformer() {
    return newInternalTransformer(XSL_TO_WSDL);
  }

  public XsltTransformer newSchemaToXsdTransformer() {
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
      return newTransformer(resolveInternalXsl(s));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public XsltTransformer newTransformer(URL url) throws IOException {
    try {
      XsltExecutable templates = xsltCache.get(url);
      if (templates == null) {
        templates = compiler.compile(new StreamSource(url.openStream()));
        xsltCache.putIfAbsent(url, templates);
      }
      return templates.load();
    } catch (SaxonApiException e) {
      throw new IOException(e);
    }
  }

  public Serializer serializer(StreamResult result, boolean formatting) throws IOException {
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
    try {
      formatXml(new StreamSource(xmlFile.toURI().toURL().openStream()), result);
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
      try (FileReader reader = new FileReader(modelFile)) {
        final Set<String> results = new LinkedHashSet<String>();
        final NodeList services = (NodeList) expression.evaluate(
          new InputSource(reader), XPathConstants.NODESET
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
      try (FileReader reader = new FileReader(modelFile)) {
        final Set<String> results = new LinkedHashSet<String>();
        final NodeList services = (NodeList) expression.evaluate(
          new InputSource(reader), XPathConstants.NODESET
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

//    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    public InternalResourceResolver(URIResolver delegate) {
      super();
      this.delegate = delegate;
    }

    public Source resolve(String href, String b) throws TransformerException {
      try {
        final URL url;
//        if (href.startsWith("cache:")) {
//          url = new URL(href.substring(6));
//        } else {
          final Matcher m = resolverPattern.matcher(href);
          if (m.matches()) {
            final ClassLoader cl = getClass().getClassLoader();
            final String rsrc = XSL_BASE + "/" + href;
            url = cl.getResource(rsrc);
          } else {
            url = null;
          }
//        }
        if (url != null) {
//          return readCached(url);
          return new StreamSource(url.openStream());
        }
      } catch (IOException e) {
        throw new TransformerException(e);
      }
      return delegate.resolve(href, b);
    }
/*
    private StreamSource readCached(URL url) throws IOException {
      final String spec = url.toExternalForm();
      if (!cache.containsKey(spec)) {
        cache.put(spec, readFully(url));
      }
      return new StreamSource(new ByteArrayInputStream(cache.get(spec)));
    }

    private byte[] readFully(URL url) throws IOException {
      final URLConnection conn = url.openConnection();
      final int len = conn.getContentLength();
      final InputStream in = conn.getInputStream();
      try (final TByteArrayOutputStream out = new TByteArrayOutputStream(len)) {
        final byte[] buffer = new byte[2048];
        int bytesRead = 0;
        for (int n = -1; (n = in.read(buffer)) > -1; bytesRead+=n) {
          out.write(buffer, 0, n);
        }
        if (bytesRead != len) {
          throw new IOException(
            "content length should have been " + len + 
            " but was actually " + bytesRead
          );
        } else if (out.get().length != bytesRead) {
          throw new IOException(
            "byte array size should have " + bytesRead + 
            " but was actually " + out.get().length
          );
        }
        return out.get();
      }
    }
*/
  }
}
