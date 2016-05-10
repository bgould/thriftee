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
/*
package org.thriftee.provider.swift;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static com.facebook.swift.parser.ThriftIdlParser.parseThriftIdl;
import static com.google.common.io.Files.asCharSource;
import static javax.xml.bind.DatatypeConverter.printLong;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.facebook.swift.parser.model.AbstractStruct;
import com.facebook.swift.parser.model.BaseType;
import com.facebook.swift.parser.model.Const;
import com.facebook.swift.parser.model.Definition;
import com.facebook.swift.parser.model.Document;
import com.facebook.swift.parser.model.Header;
import com.facebook.swift.parser.model.IdentifierType;
import com.facebook.swift.parser.model.IntegerEnum;
import com.facebook.swift.parser.model.IntegerEnumField;
import com.facebook.swift.parser.model.ListType;
import com.facebook.swift.parser.model.MapType;
import com.facebook.swift.parser.model.Service;
import com.facebook.swift.parser.model.SetType;
import com.facebook.swift.parser.model.Struct;
import com.facebook.swift.parser.model.ThriftException;
import com.facebook.swift.parser.model.ThriftField;
import com.facebook.swift.parser.model.ThriftMethod;
import com.facebook.swift.parser.model.ThriftType;
import com.facebook.swift.parser.model.TypeAnnotation;
import com.facebook.swift.parser.model.Typedef;
import com.facebook.swift.parser.model.Union;
import com.facebook.swift.parser.model.VoidType;

public class SwiftParserXML {

  public static final String NS = "http://thrift.apache.org/xml/idl";

  private XMLStreamWriter writer;

  private Document document;

  private String module;

  public String export(File root, Charset charset) throws IOException {
    final StringWriter w = new StringWriter();
    export(root, charset, new StreamResult(w));
    return w.toString();
  }

  public void export(File root, Charset charset, Result result)
      throws IOException {
    try {
      writer = XMLOutputFactory.newFactory().createXMLStreamWriter(result);
      writer.writeStartDocument("utf-8", "1.0");
      writer.writeStartElement("idl", "idl", NS);
      writer.writeNamespace("idl", NS);
      final Set<String> alreadyIncluded = new HashSet<>();
      final Queue<File> filesToInclude = new LinkedList<>();
      filesToInclude.add(root);
      for (; !filesToInclude.isEmpty(); ) {
        final File file = filesToInclude.poll();
        module = moduleNameFor(file);
        if (!alreadyIncluded.contains(module)) {
          document = parseThriftIdl(asCharSource(file, charset));
          writer.writeStartElement("idl", "document", NS);
          writer.writeAttribute("targetNamespace", namespaceUri());
          writer.writeNamespace(module, namespaceUri());
          writer.writeAttribute("name", module);
          writeHeader();
          writeDefinitions();
          writer.writeEndElement();
          alreadyIncluded.add(module);
          for (String include : document.getHeader().getIncludes()) {
            final File includedFile = new File(file.getParentFile(), include);
            filesToInclude.add(includedFile);
          }
        }
      }
      writer.writeEndElement();
      writer.writeEndDocument();
      writer.flush();
    } catch (XMLStreamException e) {
      throw new IOException(e);
    } finally {
      if (writer != null) {
        try { writer.close(); } catch (XMLStreamException e) {}
        writer = null;
      }
      document = null;
      module = null;
    }
  }

  protected static String moduleNameFor(File file) {
    final String name = file.getName();
    final int lastdot = name.lastIndexOf('.');
    final String rslt = name.substring(0, lastdot);
    if (rslt.indexOf('.') > -1) {
      throw new IllegalArgumentException(
        "file names with multiple '.' not supported yet"
      );
    }
    return rslt;
  }

  protected String namespaceUri() {
    return "http://thrift.apache.org/xml/ns/" + module;
  }

  protected String namespaceUri(String serviceName) {
    String ns = namespaceUri();
    if (!ns.endsWith("/")) {
      ns += "/";
    }
    return ns + serviceName;
  }

  protected void writeHeader() throws IOException {
    final Header header = document.getHeader();
    for (final String s : header.getIncludes()) {
      writeStartElement("include");
      writeAttribute("name", moduleNameFor(new File(s)));
      writeAttribute("file", s);
      writeEndElement();
    }
    for (final Entry<String, String> e : header.getNamespaces().entrySet()) {
      writeStartElement("namespace");
      writeAttribute("name", e.getKey());
      writeAttribute("value", e.getValue());
      writeEndElement();
    }
  }

  public void writeDefinitions() throws IOException {
    for (Definition definition : document.getDefinitions()) {
      if (definition instanceof Service) {
        write((Service) definition);
      } else if (definition instanceof Struct) {
        write((Struct) definition);
      } else if (definition instanceof Union) {
        write((Union) definition);
      } else if (definition instanceof IntegerEnum) {
        write((IntegerEnum) definition);
      } else if (definition instanceof ThriftException) {
        write((ThriftException) definition);
      } else if (definition instanceof Typedef){
        write((Typedef) definition);
      } else if (definition instanceof Const) {
        // TODO: support constants
      } else {
        throw new RuntimeException(
          "Unknown class for definition: " + definition.getClass()
        );
      }
    }
  }

  private void write(ThriftMethod def) throws IOException {
    writeStartElement("method");
    writeAttribute("name", def.getName());
    if (def.isOneway()) {
      writeAttribute("oneway", "true");
    }
    writeStartElement("returns");
    writeType(def.getReturnType());
    writeEndElement();
    for (ThriftField arg : def.getArguments()) {
      write("arg", arg);
    }
    for (ThriftField ex : def.getThrowsFields()) {
      write("throws", ex);
    }
    write(def.getAnnotations());
    writeEndElement();
  }

  private void write(String el, ThriftField def) throws IOException {
    writeStartElement(el);
    writeAttribute("name", def.getName());
    if (def.getIdentifier().isPresent()) {
      writeAttribute("field-id", printLong(def.getIdentifier().get()));
    }
    writeType(def.getType());
    writeEndElement();
  }

  protected void write(Typedef def) throws IOException {
    writeStartElement("typedef");
    writeAttribute("name", def.getName());
    writeType(def.getType());
    writeEndElement();
  }

  protected void write(ThriftException def) throws IOException {
    writeStartElement("exception");
    writeStruct(def);
    writeEndElement();
  }

  protected void write(IntegerEnum def) throws IOException {
    writeStartElement("enum");
    writeAttribute("name", def.getName());
    for (IntegerEnumField field : def.getFields()) {
      writeStartElement("member");
      writeAttribute("name", field.getName());
      writeAttribute("value", printLong(field.getValue()));
      writeEndElement();
    }
    writeEndElement();
  }

  protected void write(Union def) throws IOException {
    writeStartElement("union");
    writeStruct(def);
    writeEndElement();
  }

  protected void write(Struct def) throws IOException {
    writeStartElement("struct");
    writeStruct(def);
    writeEndElement();
  }

  protected void write(Service def) throws IOException {
    final String targetNamespace = namespaceUri(def.getName());
    writeStartElement("service");
    writeAttribute("name", def.getName());
    writeAttribute("targetNamespace", targetNamespace);
    try {
      writer.writeNamespace("tns", targetNamespace);
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
    if (def.getParent().isPresent()) {
      writeId("parent", def.getParent().get());
    }
    for (ThriftMethod method : def.getMethods()) {
      write(method);
    }
    writeEndElement();
  }

  protected void writeStruct(AbstractStruct def) throws IOException {
    writeAttribute("name", def.getName());
    for (ThriftField field : def.getFields()) {
      write("field", field);
    }
    write(def.getAnnotations());
  }

  protected void write(List<TypeAnnotation> annotations) throws IOException {
    for (TypeAnnotation annotation : annotations) {
      write(annotation);
    }
  }

  protected void write(TypeAnnotation annotation) throws IOException {
    writeStartElement("annotation");
    writeAttribute("key", annotation.getName());
    writeAttribute("value", annotation.getValue());
    writeEndElement();
  }

  protected void writeType(ThriftType type) throws IOException {
    if (type instanceof BaseType) {
      write((BaseType) type);
    } else
    if (type instanceof IdentifierType) {
      write((IdentifierType) type);
    } else
    if (type instanceof MapType) {
      write((MapType) type);
    } else
    if (type instanceof ListType) {
      write((ListType) type);
    } else
    if (type instanceof SetType) {
      write((SetType) type);
    } else
    if (type instanceof VoidType) {
      write((VoidType) type);
    } else
    {
      throw new IllegalArgumentException("Unhandled ThriftType: " + type);
    }
  }

  protected void writeId(final String pr, final String nm) throws IOException {
    final int theDot = nm.indexOf('.');
    if (theDot > -1) {
      writeAttribute(pr + "-module", nm.substring(0, theDot));
      writeAttribute(pr + "-id", nm.substring(theDot + 1));
    } else {
      writeAttribute(pr + "-module", module);
      writeAttribute(pr + "-id", nm);
    }
  }

  protected void write(MapType type) throws IOException {
    writeAttribute("type", "map");
    writeStartElement("keyType");
    writeType(type.getKeyType());
    writeEndElement();
    writeStartElement("valueType");
    writeType(type.getValueType());
    writeEndElement();
  }

  protected void write(BaseType type) throws IOException {
    writeAttribute("type", type.getType().name().toLowerCase());
  }

  protected void write(ListType type) throws IOException {
    writeAttribute("type", "list");
    writeStartElement("elemType");
    writeType(type.getElementType());
    writeEndElement();
  }

  protected void write(SetType type) throws IOException {
    writeAttribute("type", "set");
    writeStartElement("elemType");
    writeType(type.getElementType());
    writeEndElement();
  }

  protected void write(IdentifierType type) throws IOException {
    writeAttribute("type", "id");
    writeId("type", type.getName());
  }

  protected void write(VoidType type) throws IOException {
    writeAttribute("type", "void");
  }

  protected void writeStartElement(String localName) throws IOException {
    try {
      writer.writeStartElement("idl", localName, NS);
    } catch (XMLStreamException e) {
      throw new IOException(e);
    }
  }

  protected void writeEndElement() throws IOException {
    try {
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeAttribute(String name, String value) throws IOException {
    try {
      writer.writeAttribute(name, value);
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeCharacters(String chars) throws IOException {
    try {
      writer.writeCharacters(chars);
    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
  }

  public URL schemaUrl() {
    final String xsd = "org/thriftee/thrift/xml/thrift-idl.xsd";
    final URL result = getClass().getClassLoader().getResource(xsd);
    if (result == null) {
      throw new IllegalStateException("Could not load resource (null): " + xsd);
    }
    return result;
  }

  public String validate(final String str) throws SAXException, IOException {
    return validate(schemaUrl(), new StreamSource(new StringReader(str)));
  }

  public static String formatXml(String s) {
    try {
      final String indentAmount = "{http://xml.apache.org/xslt}indent-amount";
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(indentAmount, "2");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StreamResult result = new StreamResult(new StringWriter());
      StreamSource source = new StreamSource(new StringReader(s));
      transformer.transform(source, result);
      String xmlString = result.getWriter().toString();
      return xmlString;
    } catch (RuntimeException e) {
      System.out.println(s);
      throw e;
    } catch (Exception e) {
      System.out.println(s);
      throw new RuntimeException(e);
    }
  }

  public static String validate(final URL schemaUrl, final Source source) 
          throws SAXException, IOException {
    final SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
    final Schema schema = sf.newSchema(schemaUrl);
    final Validator validator = schema.newValidator();
    try {
      validator.validate(source);
      return null;
    } catch (SAXParseException e) {
      return String.format(
        "%nParse error:%n------------%n" + 
        "line number: %s%n" +
        " col number: %s%n" +
        "  system id: %s%n" +
        "  public id: %s%n" +
        "    message: %s%n",
        e.getLineNumber(), 
        e.getColumnNumber(), 
        e.getSystemId(),
        e.getPublicId(),
        e.getLocalizedMessage()
      );
    }
  }

}
*/