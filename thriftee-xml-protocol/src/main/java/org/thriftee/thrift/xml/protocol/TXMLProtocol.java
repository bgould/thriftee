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

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.thrift.protocol.TType.MAP;
import static org.apache.thrift.protocol.TType.STOP;
import static org.apache.thrift.protocol.TType.STRUCT;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.transport.TTransport;
import org.thriftee.thrift.xml.transport.TTransportInputStream;
import org.thriftee.thrift.xml.transport.TTransportOutputStream;
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

  public static final String NAMESPACE = "http://thriftee.org/xml/protocol";
  public static final String ATTRIBUTE_TYPE =       "t";
  public static final String ATTRIBUTE_KEY_TYPE =   "k";
  public static final String ATTRIBUTE_VALUE_TYPE = "v";
  public static final String ATTRIBUTE_SIZE =       "z";
  public static final String ATTRIBUTE_NAME =       "n";
  public static final String ATTRIBUTE_ID =         "i";
  public static final String ATTRIBUTE_SEQID =      "q";

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

  public void flush() throws TXMLException {
    if (__writer != null) {
      try {
        __writer.flush();
      } catch (XMLStreamException e) {
        throw ex(e);
      }
    }
  }

  protected TXMLException ex(String msg) {
    return new TXMLException(msg);
  }

  protected TXMLException ex(Throwable t) {
    final String msg = t.getMessage();
     return ex(msg, t);
  }

  protected TXMLException ex(String msg, Throwable t) {
    LOG.error("An error occurred during TXMLProtocol processing: " + msg, t);
    return ex(msg == null ? "<message was null>" : t.getMessage());
  }

  public abstract class XMLValueHolderContext
      extends AbstractContext
      implements ValueHolderContext {

    public XMLValueHolderContext(Context context) {
      super(context);
    }

    @Override
    public void writeBinary(ByteBuffer buffer) throws TXMLException {
      writeCharacters(DatatypeConverter.printBase64Binary(buffer.array()));
    }

    @Override
    public void writeBool(boolean bool) throws TXMLException {
      writeCharacters(bool ? "1" : "0");
    }

    @Override
    public void writeByte(byte bite) throws TXMLException {
      writeCharacters(DatatypeConverter.printByte(bite));
    }

    @Override
    public void writeDouble(double dbl) throws TXMLException {
      writeCharacters(DatatypeConverter.printDouble(dbl));
    }

    @Override
    public void writeI16(short i16) throws TXMLException {
      writeCharacters(DatatypeConverter.printShort(i16));
    }

    @Override
    public void writeI32(int i32) throws TXMLException {
      writeCharacters(DatatypeConverter.printInt(i32));
    }

    @Override
    public void writeI64(long i64) throws TXMLException {
      writeCharacters(DatatypeConverter.printLong(i64));
    }

    @Override
    public void writeString(String str) throws TXMLException {
      writeCharacters(str);
    }

    @Override
    public String readString() throws TXMLException {
      final String result = readCharacters();
      return result;
    }

    @Override
    public byte readByte() throws TXMLException {
      final byte result = DatatypeConverter.parseByte(readCharacters());
      return result;
    }

    @Override
    public short readI16() throws TXMLException {
      final short result = DatatypeConverter.parseShort(readCharacters());
      return result;
    }

    @Override
    public int readI32() throws TXMLException {
      final int result = DatatypeConverter.parseInt(readCharacters());
      return result;
    }

    @Override
    public long readI64() throws TXMLException {
      final long result = DatatypeConverter.parseLong(readCharacters());
      return result;
    }

    @Override
    public double readDouble() throws TXMLException {
      final double result = DatatypeConverter.parseDouble(readCharacters());
      return result;
    }

    @Override
    public ByteBuffer readBinary() throws TXMLException {
      final byte[] val = DatatypeConverter.parseBase64Binary(readCharacters());
      return ByteBuffer.wrap(val);
    }

    @Override
    public boolean readBool() throws TXMLException {
      final boolean result = "1".equals(readCharacters());
      return result;
    }

    protected void writeCharacters(String s) throws TXMLException {
      try {
        writer().writeCharacters(s);
      } catch (XMLStreamException e) {
        throw ex(e);
      }
    }

    protected boolean charsRead = false;

    protected String readCharacters() throws TXMLException {
      expectStartElement();
      try {
        final String text = reader().getElementText();
        charsRead = true;
        return text;
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
    public void popped() throws TXMLException {
      flush();
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
    public XMLMessageContext writeStart() throws TXMLException {
      writeStartElement(byteToMessageType(type));
      writeAttribute("xmlns", NAMESPACE);
      writeAttribute(ATTRIBUTE_NAME, name);
      writeAttribute(ATTRIBUTE_SEQID, Integer.toString(seqid));
      return this;
    }

    @Override
    public XMLMessageContext readStart() throws TXMLException {
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
    public XMLMessageContext writeEnd() throws TXMLException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLMessageContext readEnd() throws TXMLException {
      expectEndElement();
      return this;
    }

    @Override
    public TMessage emit() {
      return new TMessage(name, type, seqid);
    }

    @Override
    public void read(TMessage msg) {
      this.name = msg.name;
      this.seqid = msg.seqid;
      this.type = msg.type;
    }

    @Override
    public StructContext newStruct() {
      return new XMLStructContext(this);
    }

  }

  public class XMLStructContext
        extends AbstractStructContext
        implements StructContext {

    public XMLStructContext(Context parent) {
      super(parent);
    }

    @Override
    public StructContext writeStart() throws TXMLException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        writeStartElement(byteToElement(STRUCT));
        if (parent instanceof BaseContext) {
          writeAttribute("xmlns", NAMESPACE);
        }
      }
      return this;
    }

    @Override
    public StructContext writeEnd() throws TXMLException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        writeEndElement();
      }
      return this;
    }

    @Override
    public StructContext writeFieldStop() throws TXMLException {
      return this;
    }

    @Override
    public XMLStructContext readStart() throws TXMLException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        nextStartElement();
      }
      return this;
    }

    @Override
    public XMLStructContext readEnd() throws TXMLException {
      return this;
    }

    @Override
    public XMLFieldContext newField() throws TXMLException {
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
    public void read(TField field) {
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
    public XMLFieldContext writeStart() throws TXMLException {
      writeStartElement(byteToElement(type));
      writeAttribute(ATTRIBUTE_ID, Short.toString(id));
      return this;
    }

    @Override
    public XMLFieldContext writeEnd() throws TXMLException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLFieldContext readStart() throws TXMLException {
      int eventType = readerNext();
      if (eventType == CHARACTERS) {
        eventType = readerNext();
      }
      if (eventType == START_ELEMENT) {
        this.type = elementToByte(reader().getLocalName());
        this.id = readShortAttribute(ATTRIBUTE_ID);
      } else {
        this.type = STOP;
        this.id = 0;
      }
      return this;
    }

    @Override
    public XMLFieldContext readEnd() throws TXMLException {
      // stop fields don't exist, so can't read the end element
      // structs should already have had their end element as their stop field
      if (!charsRead && this.type != STOP && this.type != STRUCT) {
        // TODO: nextEndElement() below only seems necessary to finish off a
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
    public ContainerContext<T> writeStart() throws TXMLException {
      if (parent() instanceof ContainerContext<?>) {
        writeStartElement(byteToElement(containerType.byteval()));
      }
      writeAttribute(ATTRIBUTE_SIZE, Integer.toString(size));
      writeAttribute(ATTRIBUTE_VALUE_TYPE, byteToElement(elemType));
      return this;
    }

    @Override
    public ContainerContext<T> writeEnd() throws TXMLException {
      if (parent() instanceof ContainerContext<?>) {
        writeEndElement();
      }
      return this;
    }

    @Override
    public ContainerContext<T> readStart() throws TXMLException {
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
    public ContainerContext<T> readEnd() throws TXMLException {
      if (parent() instanceof ContainerContext<?>) {
        nextEndElement();
      }
      return this;
    }

    @Override
    protected void writeCharacters(String chars) throws TXMLException {
      writeStartElement(byteToElement(currtype()));
      super.writeCharacters(chars);
      writeEndElement();
    }

    @Override
    protected String readCharacters() throws TXMLException {
      nextStartElement(byteToElement(currtype()));
      final String result = super.readCharacters();
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
    public void read(TList obj) {
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
    public void read(TSet set) {
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
    public void read(TMap obj) {
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
    public MapContext writeStart() throws TXMLException {
      super.writeStart();
      writeAttribute(ATTRIBUTE_KEY_TYPE, byteToElement(keyType));
      return this;
    }

    @Override
    public void writeCharacters(String s) throws TXMLException {
      super.writeCharacters(s);
      childCount++;
    }

    @Override
    protected String readCharacters() throws TXMLException {
      final String result = super.readCharacters();
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

  protected XMLStreamReader reader() throws TXMLException {
    if (__reader == null) {
      try {
        final XMLStreamReader reader = xmlInputFactory().createXMLStreamReader(
          new TTransportInputStream(getTransport())
        );
        __reader = reader;
      } catch (XMLStreamException e) {
        throw ex(e);
      }
    }
    return __reader;
  }

  private XMLStreamWriter __writer;

  private XMLStreamReader __reader;

  private final byte elementToByte(String element) throws TXMLException {
    return ((byte)(element.charAt(0)&15));
  }

  private final String byteToElement(byte type) throws TXMLException {
    // note: this is incorrect for for type == 0, but stop fields are not used
    // todo: add check for type == 0 if that becomes an issue
    return String.valueOf((char)((type&15)+96));
  }

  private final byte messageTypeToByte(String element) throws TXMLException {
    return ((byte)(element.charAt(0)&7));
  }

  private final String byteToMessageType(byte type) throws TXMLException {
    return String.valueOf((char)((type&15)+112));
  }

  protected final void writeStartElement(String name) throws TXMLException {
    try {
      writer().writeStartElement(name);
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected void writeAttribute(String name, String val) throws TXMLException {
    try {
      writer().writeAttribute(name, val);
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected void writeEndElement() throws TXMLException {
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

  protected final String expectStartElement(String tag) throws TXMLException {
    final String actualtag = expectStartElement();
    if (!actualtag.equals(tag)) {
      throw new IllegalStateException(
        "Expected '" + tag + "' but was actually '" + actualtag + "'"
      );
    }
    return actualtag;
  }

  protected final String expectStartElement() throws TXMLException {
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

  protected final String expectEndElement(String tagname) throws TXMLException {
    final String actualtag = expectEndElement();
    if (!actualtag.equals(tagname)) {
      throw new IllegalStateException(
        "Expected '" + tagname + "' but was actually '" + actualtag + "'"
      );
    }
    return actualtag;
  }

  protected final String expectEndElement() throws TXMLException {
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

  protected final String nextStartElement(String tagname) throws TXMLException {
    readerNext();
    return expectStartElement(tagname);
  }

  protected final String nextStartElement() throws TXMLException {
    readerNext();
    return expectStartElement();
  }

  protected final String nextEndElement(String tagname) throws TXMLException {
    readerNext();
    return expectEndElement(tagname);
  }

  protected final String nextEndElement() throws TXMLException {
    readerNext();
    return expectEndElement();
  }

  protected final int readerEventType() throws TXMLException {
    return reader().getEventType();
  }

  protected final int readerNext() throws TXMLException {
    try {
      return reader().next();
    } catch (XMLStreamException e) {
      throw ex(e);
    }
  }

  protected String readAttribute(String localName) throws TXMLException {
    final String result = reader().getAttributeValue(null, localName);
    return result;
  }

  protected byte readByteAttribute(String localName) throws TXMLException {
    try {
      return Byte.valueOf(readAttribute(localName));
    } catch (TXMLException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw ex(
        "Error reading byte attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  protected short readShortAttribute(String localName) throws TXMLException {
    try {
      return Short.valueOf(readAttribute(localName));
    } catch (TXMLException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw ex(
        "Error reading short attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  protected int readIntAttribute(String localName) throws TXMLException {
    try {
      return Integer.parseInt(readAttribute(localName));
    } catch (TXMLException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw ex(
        "Error reading int attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  public static class TXMLException extends TProtocolException {

    private static final long serialVersionUID = 5007685985697860252L;

    public TXMLException(String message) {
      super(message);
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
  }

}
