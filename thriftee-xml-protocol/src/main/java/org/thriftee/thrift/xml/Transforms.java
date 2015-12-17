package org.thriftee.thrift.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.thrift.TByteArrayOutputStream;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Transforms {

  private final TransformerFactory factory;

  private final ConcurrentMap<URL, Templates> xsltCache;

  public static final String XSL_BASE = "org/thriftee/thrift/xml/protocol";

  public static final String XSL_TO_SIMPLE = "thrift-streaming-to-simple.xsl";

  public static final String XSL_TO_STREAM = "thrift-simple-to-streaming.xsl";

  public static final String XSL_TO_SCHEMA = "thrift-model-to-xsd.xsl";

  public static final String XSL_TO_WSDL   = "thrift-model-to-wsdl.xsl";

  public Transforms() {
    factory = TransformerFactory.newInstance();
    final URIResolver resolver = factory.getURIResolver();
    factory.setURIResolver(new InternalResourceResolver(resolver));
    xsltCache = new ConcurrentHashMap<>();
  }

  public Transformer newSimpleToStreamingTransformer() {
    return newInternalTransformer(XSL_TO_STREAM);
  }

  public Transformer newSimpleToStreamingTransformer(
      final File modelFile, final String module) {
    final Transformer result = newSimpleToStreamingTransformer();
    try {
      result.setParameter("schema", modelFile.toURI().toURL().toString());
      result.setParameter("root_module", module);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public void transformSimpleToStreaming(
        final File modelFile, final String module, 
        final Source src, final Result res
      ) throws TransformerException {
    newSimpleToStreamingTransformer(modelFile, module).transform(src, res);
  }

  public Transformer newStreamingToSimpleTransformer() {
    return newInternalTransformer(XSL_TO_SIMPLE);
  }

  public Transformer newStreamingToSimpleTransformer(
      final File modelFile, final String module, final String service) {
    final Transformer result = newStreamingToSimpleTransformer();
    try {
      result.setParameter("schema", "cache:" + modelFile.toURI().toURL().toString());
      result.setParameter("root_module", module);
      result.setParameter("service_name", service);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public void transformStreamingToSimple(
        final File modelFile, 
        final String mod, final String svc, 
        final Source src, final Result res
      ) throws TransformerException {
    newStreamingToSimpleTransformer(modelFile, mod, svc).transform(src, res);
  }

  public Transformer newSchemaToWsdlTransformer() {
    return newInternalTransformer(XSL_TO_WSDL);
  }

  public Transformer newSchemaToXsdTransformer() {
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

  protected Transformer newInternalTransformer(String s) {
    try {
      return newTransformer(resolveInternalXsl(s));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Transformer newTransformer(URL url) throws IOException {
    try {
      Templates templates = xsltCache.get(url);
      if (templates == null) {
        templates = factory.newTemplates(new StreamSource(url.openStream()));
        xsltCache.putIfAbsent(url, templates);
      }
      return templates.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public void formatXml(Source source, Result result) {
    try {
      final Transformer tr = factory.newTransformer();
      tr.setOutputProperty(OutputKeys.INDENT, "yes");
      tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      tr.transform(source, result);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  public static Transformer addFormatting(Transformer tr) {
    tr.setOutputProperty(OutputKeys.INDENT, "yes");
    tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    return tr;
  }

  public void formatXml(String xml, Result result) {
    formatXml(new StreamSource(new StringReader(xml)), result);
  }

  public void formatXml(File xmlFile, Result result) {
    try {
      formatXml(new StreamSource(xmlFile.toURI().toURL().openStream()), result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Map<String, File> exportWsdls(File model, File tmp) throws IOException {
    final Map<String, File> wsdlFiles = new TreeMap<>();
    final Set<String> modules = moduleNamesFor(model);
    for (final String module : modules) {
      final Set<String> services = serviceNamesFor(module, model);
      final Transformer trans = newSchemaToWsdlTransformer();
      for (String service : services) {
        final String basename = module + "." + service;
        final File wsdlOutput = new File(tmp, basename + ".wsdl");
        trans.setParameter("service_module", module);
        trans.setParameter("service_name", service);
        final StreamSource source = new StreamSource(model);
        final StreamResult result = new StreamResult(wsdlOutput);
        try {
          trans.transform(source, result);
        } catch (TransformerException e) {
          throw new IOException(e);
        } finally {
          trans.clearParameters();
        }
        wsdlFiles.put(basename, wsdlOutput);
      }
    }
    return Collections.unmodifiableMap(wsdlFiles);
  }

  public Map<String, File> exportSchemas(File model, File tmp) throws IOException {
    final Map<String, File> xsdFiles = new TreeMap<>();
    final Transformer trans = newSchemaToXsdTransformer();
    final Set<String> modules = moduleNamesFor(model);
    for (String module : modules) {
      final File schemaOutput = new File(tmp, module + ".xsd");
      final StreamSource source = new StreamSource(model);
      final StreamResult result = new StreamResult(schemaOutput);
      try {
        trans.setParameter("root_module", module);
        trans.transform(source, result);
      } catch (TransformerException e) {
        throw new IOException(e);
      } finally {
        trans.clearParameters();
      }
      xsdFiles.put(module, schemaOutput);
    }
    return Collections.unmodifiableMap(xsdFiles);
  }

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

  public String formatXml(Source source) {
    final StringWriter w = new StringWriter();
    final StreamResult result = new StreamResult(w);
    formatXml(source, result);
    return w.toString();
  }

  public String formatXml(String xml) {
    return formatXml(new StreamSource(new StringReader(xml)));
  }

  public static class InternalResourceResolver implements URIResolver {

    private final URIResolver delegate;

    private final Pattern resolverPattern = Pattern.compile("^thrift-.+xsl$");

    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    public InternalResourceResolver(URIResolver delegate) {
      super();
      this.delegate = delegate;
    }

    public Source resolve(String href, String b) throws TransformerException {
      try {
        final URL url;
        if (href.startsWith("cache:")) {
          url = new URL(href.substring(6));
        } else {
          final Matcher m = resolverPattern.matcher(href);
          if (m.matches()) {
            final ClassLoader cl = getClass().getClassLoader();
            final String rsrc = XSL_BASE + "/" + href;
            url = cl.getResource(rsrc);
          } else {
            url = null;
          }
        }
        if (url != null) {
          return readCached(url);
        }
      } catch (IOException e) {
        throw new TransformerException(e);
      }
      return delegate.resolve(href, b);
    }

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
  }
}
