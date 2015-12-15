package org.thriftee.thrift.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
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

  public Transformer newStreamingToSimpleTransformer() {
    return newInternalTransformer(XSL_TO_SIMPLE);
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

    public InternalResourceResolver(URIResolver delegate) {
      super();
      this.delegate = delegate;
    }

    public Source resolve(String href, String b) throws TransformerException {
      try {
        final Matcher m = resolverPattern.matcher(href);
        if (m.matches()) {
          final ClassLoader cl = getClass().getClassLoader();
          final String rsrc = XSL_BASE + "/" + href;
          final URL url = cl.getResource(rsrc);
          if (url != null) {
            return new StreamSource(url.openStream());
          }
        }
      } catch (IOException e) {
        throw new TransformerException(e);
      }
      return delegate.resolve(href, b);
    }

  }
}
