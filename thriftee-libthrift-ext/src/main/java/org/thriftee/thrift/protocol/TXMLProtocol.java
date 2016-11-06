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
package org.thriftee.thrift.protocol;

import static java.lang.Character.isHighSurrogate;
import static java.lang.Character.isLowSurrogate;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.thrift.protocol.TType.MAP;
import static org.apache.thrift.protocol.TType.STOP;
import static org.apache.thrift.protocol.TType.STRUCT;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.thriftee.thrift.transport.TTransportInputStream;
import org.thriftee.thrift.transport.TTransportOutputStream;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TXMLProtocol extends AbstractContextProtocol {

  public static class Factory implements TProtocolFactory {

    private static final long serialVersionUID = 1017378360734059748L;

    @Override
    public TProtocol getProtocol(TTransport transport) {
      return new TXMLProtocol(transport);
    }

  }

  public static final String ATTRIBUTE_TYPE =       "t";
  public static final String ATTRIBUTE_KEY_TYPE =   "k";
  public static final String ATTRIBUTE_VALUE_TYPE = "v";
  public static final String ATTRIBUTE_SIZE =       "z";
  public static final String ATTRIBUTE_NAME =       "n";
  public static final String ATTRIBUTE_ID =         "i";
  public static final String ATTRIBUTE_SEQID =      "q";
  public static final String ATTRIBUTE_BINARY =     "b";

  public static final String ATTRVALUE_BASE64 =     "1";

  public TXMLProtocol(TTransport trans) {
    super(trans);
  }

  public URL schemaUrl() {
    final String xsd = "thrift-xml-protocol.xsd";
    final URL result = getClass().getResource(xsd);
    if (result == null) {
      throw new IllegalStateException("Could not load resource (null): " + xsd);
    }
    return result;
  }

  public String validate(final String str) throws SAXException, IOException {
    return XML.validate(schemaUrl(), new StreamSource(new StringReader(str)));
  }

  public void flush() throws TException {
    if (__writer != null) {
      try {
        __writer.flush();
      } catch (XMLStreamException e) {
        throw ex(e);
      }
    }
  }

  public abstract class XMLValueHolderContext
      extends AbstractContext
      implements ValueHolderContext {

    public XMLValueHolderContext(Context context) {
      super(context);
    }

    @Override
    public final void writeBinary(ByteBuffer buffer) throws TException {
      writeCharacters(printBase64Binary(buffer.array()));
    }

    @Override
    public final void writeBool(boolean bool) throws TException {
      writeCharacters(bool ? "1" : "0");
    }

    @Override
    public final void writeByte(byte bite) throws TException {
      writeCharacters(DatatypeConverter.printByte(bite));
    }

    @Override
    public final void writeDouble(double dbl) throws TException {
      writeCharacters(DatatypeConverter.printDouble(dbl));
    }

    @Override
    public final void writeI16(short i16) throws TException {
      writeCharacters(DatatypeConverter.printShort(i16));
    }

    @Override
    public final void writeI32(int i32) throws TException {
      writeCharacters(DatatypeConverter.printInt(i32));
    }

    @Override
    public final void writeI64(long i64) throws TException {
      writeCharacters(DatatypeConverter.printLong(i64));
    }

    @Override
    public final void writeString(String str) throws TException {
      writeCharacters(str, true);
    }

    @Override
    public final String readString() throws TException {
      final String result = readCharacters(true);
      return result;
    }

    @Override
    public final byte readByte() throws TException {
      final byte result = DatatypeConverter.parseByte(readCharacters());
      return result;
    }

    @Override
    public final short readI16() throws TException {
      final short result = DatatypeConverter.parseShort(readCharacters());
      return result;
    }

    @Override
    public final int readI32() throws TException {
      final int result = DatatypeConverter.parseInt(readCharacters());
      return result;
    }

    @Override
    public final long readI64() throws TException {
      final long result = DatatypeConverter.parseLong(readCharacters());
      return result;
    }

    @Override
    public final double readDouble() throws TException {
      final double result = DatatypeConverter.parseDouble(readCharacters());
      return result;
    }

    @Override
    public final ByteBuffer readBinary() throws TException {
      final byte[] val = parseBase64Binary(readCharacters());
      return ByteBuffer.wrap(val);
    }

    @Override
    public final boolean readBool() throws TException {
      final boolean result = "1".equals(readCharacters());
      return result;
    }

    protected void writeCharacters(
        final String s, final boolean check) throws TException {
      final String charsToWrite;
      if (check && XML.hasInvalidChars(s)) {
        writeAttribute(ATTRIBUTE_BINARY, ATTRVALUE_BASE64);
        try {
          charsToWrite = printBase64Binary(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
          throw ex(e);
        }
      } else {
        charsToWrite = s;
      }
      try {
        writer().writeCharacters(charsToWrite);
      } catch (XMLStreamException e) {
        throw ex(e);
      }
    }

    private final void writeCharacters(String s) throws TException {
      writeCharacters(s, false);
    }

    protected boolean charsRead = false;

    private final String readCharacters() throws TException {
      return readCharacters(false);
    }

    protected String readCharacters(boolean check) throws TException {
      expectStartElement();
      boolean base64 = check && readAttribute(ATTRIBUTE_BINARY) != null;
      try {
        final String text = reader().getElementText();
        charsRead = true;
        if (base64) {
          try {
            return new String(parseBase64Binary(text), "UTF-8");
          } catch (UnsupportedEncodingException e) {
            throw ex(e);
          }
        } else {
          return text;
        }
      } catch (XMLStreamException e) {
        throw ex(e);
      }
    }

    @Override
    public XMLListContext newList() {
      return new XMLListContext(this);
    }

    @Override
    public XMLSetContext newSet() {
      return new XMLSetContext(this);
    }

    @Override
    public XMLMapContext newMap() {
      return new XMLMapContext(this);
    }

    @Override
    public XMLStructContext newStruct() {
      return new XMLStructContext(this);
    }

  }

  public class XMLBaseContext extends BaseContext {

    public XMLBaseContext(ContextType type) {
      super(type);
    }

    @Override
    public MessageContext newMessage() {
      return new XMLMessageContext(this);
    }

    @Override public StructContext newStruct() {
      return new XMLStructContext(this);
    }

    @Override
    public void popped() throws TException {
      flush();
      if (__writer != null) {
        try {
          __writer.close();
        } catch (XMLStreamException e) {
          LOG.warn("Error closing writer", e);
        } finally {
          __writer = null;
        }
      }
      if (__reader != null) {
        try {
          __reader.close();
        } catch (XMLStreamException e) {
          LOG.warn("Error closing reader", e);
        } finally {
          __reader = null;
        }
      }
    }

  }

  public class XMLMessageContext
        extends AbstractContext
        implements MessageContext {

    private String name;
    private byte type;
    private int seqid;

    public XMLMessageContext(Context parent) {
      super(parent);
    }

    @Override
    public XMLMessageContext writeStart() throws TException {
      writeStartElement(byteToMessageType(type));
      writeAttribute(ATTRIBUTE_NAME, name);
      writeAttribute(ATTRIBUTE_SEQID, Integer.toString(seqid));
      return this;
    }

    @Override
    public XMLMessageContext readStart() throws TException, TTransportException {
      final String msgname = nextStartElement();
      this.type = messageTypeToByte(msgname);
      this.name = readAttribute(ATTRIBUTE_NAME);
      // TODO: seqid should always be required
      if (reader().getAttributeValue(null, ATTRIBUTE_SEQID) != null) {
        this.seqid = readIntAttribute(ATTRIBUTE_SEQID);
      } else {
        this.seqid = 1;
      }
      return this;
    }

    @Override
    public XMLMessageContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLMessageContext readEnd() throws TException {
      expectEndElement();
      return this;
    }

    @Override
    public TMessage emit() {
      return new TMessage(name, type, seqid);
    }

    @Override
    public void set(TMessage msg) {
      this.name = msg.name;
      this.seqid = msg.seqid;
      this.type = msg.type;
    }

    @Override
    public StructContext newStruct() {
      return new XMLStructContext(this);
    }

    @Override
    public String toString() {
      return "<TMessage name:'" + name + "' type: " + type + " seqid:" + seqid + ">";
    }

  }

  public class XMLStructContext
        extends AbstractStructContext
        implements StructContext {

    public XMLStructContext(Context parent) {
      super(parent);
    }

    @Override
    public StructContext writeStart() throws TException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        writeStartElement(byteToElement(STRUCT));
      }
      return this;
    }

    @Override
    public StructContext writeEnd() throws TException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        writeEndElement();
      }
      return this;
    }

    @Override
    public StructContext writeFieldStop() throws TException {
      writeEmptyElement(byteToElement(STOP));
      return this;
    }

    @Override
    public XMLStructContext readStart() throws TException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        nextStartElement();
      }
      return this;
    }

    @Override
    public XMLStructContext readEnd() throws TException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        nextEndElement();
      }
      return this;
    }

    @Override
    public XMLFieldContext newField() throws TException {
      return new XMLFieldContext(this);
    }

  }

  public class XMLFieldContext
      extends XMLValueHolderContext
      implements FieldContext {

    private String name;
    private byte type;
    private short id;

    public XMLFieldContext(StructContext struct) {
      super(struct);
      if (struct == null) {
        throw new IllegalArgumentException("parent struct cannot be null.");
      }
    }

    @Override
    public byte fieldType() {
      return this.type;
    }

    @Override
    public void set(TField field) {
      this.name = field.name;
      this.type = field.type;
      this.id = field.id;
    }

    @Override
    public TField emit() {
      return new TField(name, type, id);
    }

    @Override
    public String toString() {
      return emit().toString();
    }

    @Override
    public XMLFieldContext writeStart() throws TException {
      writeStartElement(byteToElement(type));
      writeAttribute(ATTRIBUTE_ID, Short.toString(id));
      return this;
    }

    @Override
    public XMLFieldContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLFieldContext readStart() throws TException {
      int eventType = readerNext();
      if (eventType == CHARACTERS) {
        eventType = readerNext();
      }
      if (eventType == START_ELEMENT) {
        this.type = elementToByte(reader().getLocalName());
        this.id = this.type == STOP ? 0 : readShortAttribute(ATTRIBUTE_ID);
      }
      return this;
    }

    @Override
    public XMLFieldContext readEnd() throws TException {
      if (!charsRead) {
        nextEndElement();
      }
      return this;
    }

  }

  public abstract class XMLContainerContext<T>
      extends XMLValueHolderContext
      implements ContainerContext<T> {

    protected byte elemType;
    protected int size;
    protected final Class<T> emitType;
    protected final ContainerType containerType;

    protected XMLContainerContext(
        ValueHolderContext parent,
        Class<T> emitType,
        ContainerType containerType) {
      super(parent);
      if (parent == null) {
          throw new IllegalArgumentException("parent cannot be null.");
      }
      this.emitType = emitType;
      this.containerType = containerType;
    }

    @Override
    public String toString() {
      return "<"+emitType.getSimpleName()+" type:"+elemType+" size:"+size+">";
    }

    @Override
    public ContainerType containerType() {
      return containerType;
    }

    @Override
    public ContainerContext<T> writeStart() throws TException {
      if (parent() instanceof ContainerContext<?>) {
        writeStartElement(byteToElement(containerType.byteval()));
      }
      writeAttribute(ATTRIBUTE_SIZE, Integer.toString(size));
      writeAttribute(ATTRIBUTE_VALUE_TYPE, byteToElement(elemType));
      return this;
    }

    @Override
    public ContainerContext<T> writeEnd() throws TException {
      if (parent() instanceof ContainerContext<?>) {
        writeEndElement();
      }
      return this;
    }

    @Override
    public ContainerContext<T> readStart() throws TException {
      final String name;
      if (parent() instanceof ContainerContext<?>) {
        name = nextStartElement();
      } else {
        name = expectStartElement();
      }
      final byte xtype = containerType.byteval();
      final byte ctype = elementToByte(name);
      if (xtype != ctype) {
        throw new IllegalStateException(
          "Expected '" + xtype + "' but was '" + ctype + "'");
      }
      this.size = readIntAttribute(ATTRIBUTE_SIZE);
      this.elemType = elementToByte(readAttribute(ATTRIBUTE_VALUE_TYPE));
      if (ctype == MAP) {
        ((XMLMapContext)this).keyType = elementToByte(
          readAttribute(ATTRIBUTE_KEY_TYPE)
        );
      }
      return this;
    }

    @Override
    public ContainerContext<T> readEnd() throws TException {
      if (parent() instanceof ContainerContext<?>) {
        nextEndElement();
      }
      return this;
    }

    @Override
    protected void writeCharacters(String chars, boolean check) throws TException {
      writeStartElement(byteToElement(currtype()));
      super.writeCharacters(chars, check);
      writeEndElement();
    }

    @Override
    protected String readCharacters(boolean check) throws TException {
      nextStartElement(byteToElement(currtype()));
      final String result = super.readCharacters(check);
      expectEndElement();
      return result;
    }

    protected byte currtype() {
      return elemType;
    }

  }

  public class XMLListContext
      extends XMLContainerContext<TList>
      implements ListContext {

    public XMLListContext(ValueHolderContext field) {
      super(field, TList.class, ContainerType.LIST);
    }

    @Override
    public void set(TList obj) {
      this.elemType = obj.elemType;
      this.size = obj.size;
    }

    @Override
    public TList emit() {
      return new TList(elemType, size);
    }

  }

  public class XMLSetContext
      extends XMLContainerContext<TSet>
      implements SetContext {

    public XMLSetContext(ValueHolderContext field) {
      super(field, TSet.class, ContainerType.SET);
    }

    @Override
    public TSet emit() {
      return new TSet(elemType, size);
    }

    @Override
    public void set(TSet set) {
      this.elemType = set.elemType;
      this.size = set.size;
    }

  }

  public class XMLMapContext
      extends XMLContainerContext<TMap>
      implements MapContext {

    private byte keyType;
    private int childCount;

    public XMLMapContext(ValueHolderContext field) {
      super(field, TMap.class, ContainerType.MAP);
    }

    @Override
    public void set(TMap obj) {
      this.elemType = obj.valueType;
      this.keyType = obj.keyType;
      this.size = obj.size;
    }

    @Override
    public TMap emit() {
      return new TMap(keyType, elemType, size);
    }

    @Override
    public String toString() {
      return "<TMap key:"+keyType+" type:"+elemType+" size:"+size+">";
    }

    @Override
    public MapContext writeStart() throws TException {
      super.writeStart();
      writeAttribute(ATTRIBUTE_KEY_TYPE, byteToElement(keyType));
      return this;
    }

    @Override
    public void writeCharacters(String s, boolean check) throws TException {
      super.writeCharacters(s, check);
      childCount++;
    }

    @Override
    protected String readCharacters(boolean check) throws TException {
      final String result = super.readCharacters(check);
      childCount++;
      return result;
    }

    @Override
    public void pushed() {
      childCount++;
    }

    @Override
    protected byte currtype() {
      return childCount % 2 == 0 ? keyType : elemType;
    }

  }

  protected XMLStreamWriter writer() throws XMLStreamException {
    if (__writer == null) {
      __writer = xmlOutputFactory().createXMLStreamWriter(
        new TTransportOutputStream(getTransport())
      );
    }
    return __writer;
  }

  protected XMLStreamReader reader() throws TException {
    if (__reader == null) {
      try {
        final XMLStreamReader reader = xmlInputFactory().createXMLStreamReader(
          new TTransportInputStream(getTransport())
        );
        __reader = reader;
      } catch (XMLStreamException e) {
//        if (e.getCause() != null && e.getCause() instanceof TTransportException) {
//          throw (TTransportException) e.getCause();
//        } else {
        final TException te = ex(e);
        throw te;
//        }
      }
    }
    return __reader;
  }

  private XMLStreamWriter __writer;

  private XMLStreamReader __reader;

  public static final byte elementToByte(String element) throws TException {
    final char c = element.charAt(0);
    return (byte)((c&31)-1);
  }

  public static final String byteToElement(byte type) throws TException {
    return String.valueOf((char)((type&31)+97));
  }

  public static final byte messageTypeToByte(String element) throws TException {
    return (byte)((element.charAt(0)&7)-1);
  }

  public static final String byteToMessageType(byte type) throws TException {
    return String.valueOf((char)((type&15)+113));
  }

  protected final void writeStartElement(String name) throws TException {
    try {
      writer().writeStartElement(name);
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected void writeAttribute(String name, String val) throws TException {
    try {
      writer().writeAttribute(name, val);
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected void writeEmptyElement(String name) throws TException {
    try {
      writer().writeEmptyElement(name);
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected void writeEndElement() throws TException {
    try {
      writer().writeEndElement();
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  private static final XMLInputFactory XML_IN;

  private static final XMLOutputFactory XML_OUT;

  static {

    XML_IN = XMLInputFactory.newFactory();
    XML_IN.setProperty(XMLInputFactory.IS_COALESCING, true);
    XML_IN.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

    XML_OUT = XMLOutputFactory.newFactory();

  }

  protected XMLInputFactory xmlInputFactory() {
    return XML_IN;
  }

  protected XMLOutputFactory xmlOutputFactory() {
    return XML_OUT;
  }

  @Override
  protected BaseContext createBaseContext(ContextType type) {
    return new XMLBaseContext(type);
  }

  protected final String expectStartElement(String tag) throws TException {
    final String actualtag = expectStartElement();
    if (!actualtag.equals(tag)) {
      throw new IllegalStateException(
        "Expected '" + tag + "' but was actually '" + actualtag + "'"
      );
    }
    return actualtag;
  }

  protected final String expectStartElement() throws TException {
    final int etype = (reader().getEventType() == CHARACTERS)
                    ? (readerNext())
                    : (reader().getEventType());
    if (etype == START_ELEMENT) {
      return reader().getLocalName();
    } else {
      throw ex(
        "Expected START_ELEMENT but was " + XML.streamEventToString(etype)
      );
    }
  }

  protected final String expectEndElement(String tagname) throws TException {
    final String actualtag = expectEndElement();
    if (!actualtag.equals(tagname)) {
      throw new IllegalStateException(
        "Expected '" + tagname + "' but was actually '" + actualtag + "'"
      );
    }
    return actualtag;
  }

  protected final String expectEndElement() throws TException {
    final int etype = (reader().getEventType() == CHARACTERS)
                    ? (readerNext())
                    : (reader().getEventType());
    if (etype == END_ELEMENT) {
      return reader().getLocalName();
    } else {
      throw new IllegalStateException(
        "Expected END_ELEMENT but was " + XML.streamEventToString(etype)
      );
    }
  }

  protected final String nextStartElement(String tagname) throws TException {
    readerNext();
    return expectStartElement(tagname);
  }

  protected final String nextStartElement() throws TException {
    readerNext();
    return expectStartElement();
  }

  protected final String nextEndElement(String tagname) throws TException {
    readerNext();
    return expectEndElement(tagname);
  }

  protected final String nextEndElement() throws TException {
    readerNext();
    return expectEndElement();
  }

  protected final int readerEventType() throws TException {
    return reader().getEventType();
  }

  protected final int readerNext() throws TException {
    try {
      return reader().next();
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected String readAttribute(String localName) throws TException {
    final String result = reader().getAttributeValue(null, localName);
    return result;
  }

  protected byte readByteAttribute(String localName) throws TException {
    try {
      return Byte.valueOf(readAttribute(localName));
    } catch (TException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw ex(
        "Error reading byte attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  protected short readShortAttribute(String localName) throws TException {
    try {
      return Short.valueOf(readAttribute(localName));
    } catch (TException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw ex(
        "Error reading short attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  protected int readIntAttribute(String localName) throws TException {
    try {
      return Integer.parseInt(readAttribute(localName));
    } catch (TException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw ex(
        "Error reading int attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  public static enum XML {
    Utils;
    public static String streamEventToString(int event) {
      switch (event) {
      case XMLStreamConstants.START_ELEMENT: // 1;
        return "START_ELEMENT";
      case XMLStreamConstants.END_ELEMENT: // 2;
        return "END_ELEMENT";
      case XMLStreamConstants.PROCESSING_INSTRUCTION: // 3;
        return "PROCESSING_INSTRUCTION";
      case XMLStreamConstants.CHARACTERS: // 4;
        return "CHARACTERS";
      case XMLStreamConstants.COMMENT: // 5;
        return "COMMENT";
      case XMLStreamConstants.SPACE: // 6;
        return "SPACE";
      case XMLStreamConstants.START_DOCUMENT: // 7;
        return "START_DOCUMENT";
      case XMLStreamConstants.END_DOCUMENT: // 8;
        return "END_DOCUMENT";
      case XMLStreamConstants.ENTITY_REFERENCE: // 9;
        return "ENTITY_REFERENCE";
      case XMLStreamConstants.ATTRIBUTE: // 10;
        return "ATTRIBUTE";
      case XMLStreamConstants.DTD: // 11;
        return "DTD";
      case XMLStreamConstants.CDATA: // 12;
        return "CDATA";
      case XMLStreamConstants.NAMESPACE: // 13;
        return "NAMESPACE";
      case XMLStreamConstants.NOTATION_DECLARATION: // 14;
        return "NOTATION_DECLARATION";
      case XMLStreamConstants.ENTITY_DECLARATION: // 15;
        return "ENTITY_DECLARATION";
      default:
        throw new IllegalArgumentException();
      }
    }
    public static String dumpCurrentState(XMLStreamReader reader) {
      try {
      return String.format(
        "reader = { eventType: %s, hasNext: %s, %s }",
        XML.streamEventToString(reader.getEventType()),
        reader.hasNext(),
        reader.hasName() ? reader.getLocalName() : "..."
      );
      } catch (XMLStreamException e) {
        throw new RuntimeException(e);
      }
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
    public static final boolean hasInvalidChars(final String text) {
      if (null == text || text.isEmpty()) {
        return false;
      }
      final int len = text.length();
      char current = 0;
      int codePoint = 0;
      for (int i = 0; i < len; i++) {
        current = text.charAt(i);
        if (isHighSurrogate(current) && i + 1 < len && isLowSurrogate(text.charAt(i + 1))) {
          codePoint = text.codePointAt(i++);
        } else {
          codePoint = current;
        }
        if (!(  (codePoint ==     0x9)
            ||  (codePoint ==     0xA)
            ||  (codePoint ==     0xD)
            || ((codePoint >=    0x20) && (codePoint <=   0xD7FF))
            || ((codePoint >=  0xE000) && (codePoint <=   0xFFFD))
            || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)) )) {
          return true;
        }
      }
      return false;
    }
  }

}
