package org.thriftee.thrift.protocol;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.thrift.protocol.TMessageType.CALL;
import static org.apache.thrift.protocol.TMessageType.EXCEPTION;
import static org.apache.thrift.protocol.TMessageType.ONEWAY;
import static org.apache.thrift.protocol.TMessageType.REPLY;
import static org.apache.thrift.protocol.TType.BOOL;
import static org.apache.thrift.protocol.TType.BYTE;
import static org.apache.thrift.protocol.TType.DOUBLE;
import static org.apache.thrift.protocol.TType.ENUM;
import static org.apache.thrift.protocol.TType.I16;
import static org.apache.thrift.protocol.TType.I32;
import static org.apache.thrift.protocol.TType.I64;
import static org.apache.thrift.protocol.TType.LIST;
import static org.apache.thrift.protocol.TType.MAP;
import static org.apache.thrift.protocol.TType.SET;
import static org.apache.thrift.protocol.TType.STOP;
import static org.apache.thrift.protocol.TType.STRING;
import static org.apache.thrift.protocol.TType.STRUCT;
import static org.apache.thrift.protocol.TType.VOID;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.function.Function;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.transport.TTransport;
import org.thriftee.thrift.transport.TTransportInputStream;
import org.thriftee.thrift.transport.TTransportOutputStream;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TXMLProtocol extends AbstractContextProtocol {

  private static final String[] VERBOSE_MESSAGE_NAMES = new String[5];
  static {
    VERBOSE_MESSAGE_NAMES[CALL] = "call";
    VERBOSE_MESSAGE_NAMES[REPLY] = "reply";
    VERBOSE_MESSAGE_NAMES[EXCEPTION] = "exception";
    VERBOSE_MESSAGE_NAMES[ONEWAY] = "oneway";
  }

  private static final String[] VERBOSE_TYPE_NAMES = new String[17];
  static {
    VERBOSE_TYPE_NAMES[STOP] = "stop";
    VERBOSE_TYPE_NAMES[VOID] = "void";
    VERBOSE_TYPE_NAMES[BOOL] = "bool";
    VERBOSE_TYPE_NAMES[BYTE] = "i8";
    VERBOSE_TYPE_NAMES[DOUBLE] = "double";
    VERBOSE_TYPE_NAMES[I16] = "i16";
    VERBOSE_TYPE_NAMES[I32] = "i32";
    VERBOSE_TYPE_NAMES[I64] = "i64";
    VERBOSE_TYPE_NAMES[STRING] = "string";
    VERBOSE_TYPE_NAMES[STRUCT] = "struct";
    VERBOSE_TYPE_NAMES[MAP] = "map";
    VERBOSE_TYPE_NAMES[SET] = "set";
    VERBOSE_TYPE_NAMES[LIST] = "list";
    VERBOSE_TYPE_NAMES[ENUM] = "enum";
  }

  private static byte search(String[] vals, String s) {
    for (int i = 0; i < vals.length; i++) {
      if (s.equals(vals[i])) {
        return (byte) i;
      }
    }
    throw new IllegalArgumentException("not found: " + s);
  }

  public static enum Variant {
    CONCISE(
      "http://thrift.apache.org/xml/protocol/concise", 
      "k", 
      "v", 
      "t", 
      "z", 
      "n", 
      "i",
      null,
      "q", 
      false,
      (s) -> Byte.parseByte(s.substring(1)),
      (b) -> "t" + b,
      (s) -> Byte.parseByte(s.substring(1)),
      (b) -> "m" + b
    ),
    VERBOSE(
      "http://thrift.apache.org/xml/protocol/verbose",
      "key",
      "value",
      "type",
      "size",
      "name",
      "field",
      "fname",
      "seqid",
      true,
      (s) -> search(VERBOSE_TYPE_NAMES, s),
      (b) -> VERBOSE_TYPE_NAMES[b],
      (s) -> search(VERBOSE_MESSAGE_NAMES, s),
      (b) -> VERBOSE_MESSAGE_NAMES[b]
    ),
    ;
    public final String NAMESPACE;
    public final String ATTRIBUTE_TYPE;
    public final String ATTRIBUTE_KEY_TYPE;
    public final String ATTRIBUTE_VALUE_TYPE;
    public final String ATTRIBUTE_SIZE;
    public final String ATTRIBUTE_NAME;
    public final String ATTRIBUTE_ID;
    public final String ATTRIBUTE_FIELD_NAME;
    public final String ATTRIBUTE_SEQID;
    public final boolean INCLUDE_NAMES;
    public final Function<String, Byte> _elementToByte;
    public final Function<Byte, String> _byteToElement;
    public final Function<String, Byte> _messageTypeToByte;
    public final Function<Byte, String> _byteToMessageType;
    private Variant(
        String namespace,  
        String keyElement, 
        String valueElement,
        String typeAttribute,
        String sizeAttribute,
        String nameAttribute,
        String idAttribute,
        String fieldNameAttribute,
        String seqidAttribute,
        boolean includeNames,
        Function<String, Byte> elementToByte,
        Function<Byte, String> byteToElement,
        Function<String, Byte> messageTypeToByte,
        Function<Byte, String> byteToMessageType) {
      NAMESPACE = namespace;
      INCLUDE_NAMES = includeNames;
      ATTRIBUTE_TYPE = typeAttribute;
      ATTRIBUTE_KEY_TYPE = keyElement;
      ATTRIBUTE_VALUE_TYPE = valueElement;
      ATTRIBUTE_SIZE = sizeAttribute;
      ATTRIBUTE_NAME = nameAttribute;
      ATTRIBUTE_ID = idAttribute;
      ATTRIBUTE_FIELD_NAME = fieldNameAttribute;
      ATTRIBUTE_SEQID = seqidAttribute;
      this._elementToByte = elementToByte;
      this._byteToElement = byteToElement;
      this._messageTypeToByte = messageTypeToByte;
      this._byteToMessageType = byteToMessageType;
    }
  }

  public static class Factory implements TProtocolFactory {

    private static final long serialVersionUID = 1017378360734059748L;

    private final Variant variant;

    private final boolean validating;

    public Factory() {
      this(null, false);
    }

    public Factory(Variant variant, boolean validating) {
      if (variant == null) {
        variant = Variant.CONCISE;
      }
      this.variant = variant;
      this.validating = validating;
    }

    @Override
    public TProtocol getProtocol(TTransport transport) {
      return new TXMLProtocol(transport, variant, validating);
    }

  }

  protected final Variant variant;

  protected final boolean validating;

  public TXMLProtocol(TTransport trans, Variant variant, boolean validating) {
    super(trans);
    this.variant = variant;
    this.validating = validating;
  }

  public URL schemaUrl() {
    final String xsd = "txmlprotocol-" + variant.name().toLowerCase() + ".xsd";
    final URL result = getClass().getResource(xsd);
    if (result == null) {
      throw new IllegalStateException("Could not load resource (null): " + xsd);
    }
    return result;
  }

  public String validate(final String str) throws SAXException, IOException {
    return validate(new StreamSource(new StringReader(str)));
  }

  public String validate(final Source source) throws SAXException, IOException {
    final SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
    final Schema schema = sf.newSchema(schemaUrl());
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

  public abstract class XMLValueHolderContext 
      extends AbstractContext 
      implements ValueHolderContext {

    public XMLValueHolderContext(Context context) {
      super(context);
    }

    @Override
    public void writeBinary(ByteBuffer buffer) throws TException {
      writeCharacters(Base64.encodeBase64String(buffer.array()));
    }

    @Override
    public void writeBool(boolean bool) throws TException {
      writeCharacters(Boolean.toString(bool));
    }

    @Override
    public void writeByte(byte bite) throws TException {
      writeCharacters(Byte.toString(bite));
    }

    @Override
    public void writeDouble(double dbl) throws TException {
      writeCharacters(Double.toString(dbl));
    }

    @Override
    public void writeI16(short arg0) throws TException {
      writeCharacters(Short.toString(arg0));
    }

    @Override
    public void writeI32(int arg0) throws TException {
      writeCharacters(Integer.toString(arg0));
    }

    @Override
    public void writeI64(long arg0) throws TException {
      writeCharacters(Long.toString(arg0));
    }

    @Override
    public void writeString(String str) throws TException {
      writeCharacters(str);
    }

    @Override
    public String readString() throws TException {
      final String result = readCharacters();
      return result;
    }

    @Override
    public byte readByte() throws TException {
      final byte result = Byte.valueOf(readCharacters());
      return result;
    }

    @Override
    public short readI16() throws TException {
      final short result = Short.valueOf(readCharacters());
      return result;
    }

    @Override
    public int readI32() throws TException {
      final int result = Integer.valueOf(readCharacters());
      return result;
    }

    @Override
    public long readI64() throws TException {
      final long result = Long.valueOf(readCharacters());
      return result;
    }

    @Override
    public double readDouble() throws TException {
      final double result = Double.valueOf(readCharacters());
      return result;
    }

    @Override
    public ByteBuffer readBinary() throws TException {
      final String chars = readCharacters();
      final ByteBuffer result = ByteBuffer.wrap(Base64.decodeBase64(chars));
      return result;
    }

    @Override
    public boolean readBool() throws TException {
      final boolean result = Boolean.valueOf(readCharacters());
      return result;
    }

    protected void writeCharacters(String s) throws TException {
      try {
        writer().writeCharacters(s);
      } catch (XMLStreamException e) {
        throw wrap(e);
      }
    }

    protected String readCharacters() throws TException {
      try {
        int etype = reader().getEventType();
        if (etype == START_ELEMENT) {
          etype = reader().next();
        }
        if (etype == CHARACTERS) {
          return reader().getText();
        }
        throw new TException(
          "expected CHARACTERS but was " + XML.streamEventToString(etype));
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }

    @Override 
    public XMLListContext newList() throws TException {
      return new XMLListContext(this);
    }

    @Override 
    public XMLSetContext newSet() throws TException {
      return new XMLSetContext(this);
    }

    @Override 
    public XMLMapContext newMap() throws TException {
      return new XMLMapContext(this);
    }

    @Override
    public XMLStructContext newStruct() throws TException {
      return new XMLStructContext(this);
    }

  }

  public class XMLBaseContext extends BaseContext {

    public XMLBaseContext(ContextType type) {
      super(type);
    }

    @Override 
    public MessageContext newMessage() throws TException {
      return new XMLMessageContext(this);
    }

    @Override public StructContext newStruct() throws TException {
      return new XMLStructContext(this);
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
      writeAttribute(variant.ATTRIBUTE_NAME, name);
      writeAttribute(variant.ATTRIBUTE_SEQID, Integer.toString(seqid));
      writeAttribute("xmlns", variant.NAMESPACE);
      return this;
    }

    @Override
    public XMLMessageContext readStart() throws TException {
      final String msgname = nextStartElement();
      this.type = messageTypeToByte(msgname);
      this.name = readAttribute(variant.ATTRIBUTE_NAME);
      this.seqid = readIntAttribute(variant.ATTRIBUTE_SEQID);
      return this;
    }

    @Override
    public XMLMessageContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLMessageContext readEnd() throws TException {
      nextEndElement();
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
    public StructContext writeStart() throws TException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        writeStartElement(byteToElement(STRUCT));
        if (parent instanceof BaseContext) {
          writeAttribute("xmlns", variant.NAMESPACE);
        }
      }
      if (variant.INCLUDE_NAMES) {
        writeAttribute(variant.ATTRIBUTE_NAME, name);
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
      writeStartElement(byteToElement(STOP));
      writeEndElement();
      return this;
    }

    @Override 
    public XMLStructContext readStart() throws TException {
      final Context parent = parent();
      if (!(parent instanceof FieldContext)) {
        nextStartElement();
      }
      if (variant.INCLUDE_NAMES) {
        name = readAttribute(variant.ATTRIBUTE_NAME);
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
    public void read(TField field) {
      this.name = field.name;
      this.type = field.type;
      this.id = field.id;
    }

    @Override
    public TField emit() {
      return new TField(name, type, id);
    }

    public String toString() {
      return emit().toString();
    }

    @Override
    public XMLFieldContext writeStart() throws TException {
      writeStartElement(byteToElement(type));
      writeAttribute(variant.ATTRIBUTE_ID, Short.toString(id));
      if (variant.ATTRIBUTE_FIELD_NAME != null) {
        writeAttribute(variant.ATTRIBUTE_FIELD_NAME, name);
      }
      return this;
    }

    @Override
    public XMLFieldContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLFieldContext readStart() throws TException {
      this.type = elementToByte(nextStartElement());
      if (this.type == STOP) {
        this.id = 0;
      } else {
        this.id = readShortAttribute(variant.ATTRIBUTE_ID);
        if (variant.ATTRIBUTE_FIELD_NAME != null) {
          name = readAttribute(variant.ATTRIBUTE_FIELD_NAME);
        }
      }
      return this;
    }

    @Override
    public XMLFieldContext readEnd() throws TException {
      nextEndElement();
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

    public String toString() {
      return "<"+emitType.getSimpleName()+" type:"+elemType+" size:"+size+">";
    }

    @Override
    public ContainerType containerType() {
      return containerType;
    }

    @Override 
    public ContainerContext<T> writeStart() throws TException {
      writeAttribute(variant.ATTRIBUTE_SIZE, Integer.toString(size));
      writeAttribute(variant.ATTRIBUTE_VALUE_TYPE, byteToElement(elemType));
      return this;
    }

    @Override 
    public ContainerContext<T> writeEnd() throws TException {
      return this;
    }

    @Override 
    public ContainerContext<T> readStart() throws TException {
      final String name = expectStartElement();
      final byte xtype = containerType.byteval();
      final byte ctype = elementToByte(name);
      if (xtype != ctype) {
        throw new IllegalStateException(
          "Expected '" + xtype + "' but was '" + ctype + "'");
      }
      this.size = readIntAttribute(variant.ATTRIBUTE_SIZE);
      this.elemType = elementToByte(readAttribute(variant.ATTRIBUTE_VALUE_TYPE));
      if (ctype == MAP) {
        ((XMLMapContext)this).keyType = elementToByte( 
          readAttribute(variant.ATTRIBUTE_KEY_TYPE)
        );
      }
      return this;
    }

    @Override
    public ContainerContext<T> readEnd() throws TException {
      return this;
    }

    @Override
    protected void writeCharacters(String chars) throws TException {
      writeStartElement(byteToElement(currtype()));
      super.writeCharacters(chars);
      writeEndElement();
    }

    @Override
    protected String readCharacters() throws TException {
      nextStartElement(byteToElement(currtype()));
      final String result = super.readCharacters();
      nextEndElement();
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
    public MapContext writeStart() throws TException {
      super.writeStart();
      writeAttribute(variant.ATTRIBUTE_KEY_TYPE, byteToElement(keyType));
      return this;
    }

    @Override
    public void writeCharacters(String s) throws TException {
      super.writeCharacters(s);
      childCount++;
    }

    @Override
    protected String readCharacters() throws TException {
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

  protected XMLStreamReader reader() throws TException {
    if (__reader == null) {
      try {
        final XMLStreamReader reader = xmlInputFactory().createXMLStreamReader(
          new TTransportInputStream(getTransport())
        );
        __reader = reader;
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }
    return __reader;
  }

  private XMLStreamWriter __writer;

  private XMLStreamReader __reader;

  protected TException wrap(XMLStreamException e) throws TException {
    throw new TException(e);
  }

  protected byte elementToByte(String element) {
    return variant._elementToByte.apply(element);
  }

  protected String byteToElement(byte type) {
    return variant._byteToElement.apply(type);
  }

  protected byte messageTypeToByte(String element) {
    return variant._messageTypeToByte.apply(element);
  }

  protected String byteToMessageType(byte type) {
    return variant._byteToMessageType.apply(type);
  }

  protected void writeStartElement(String name) throws TException {
    try {
      writer().writeStartElement(name);
    } catch (XMLStreamException e) {
      throw wrap(e);
    }
  }

  protected void writeAttribute(String name, String value) throws TException {
    try {
      writer().writeAttribute(name, value);
    } catch (XMLStreamException e) {
      throw wrap(e);
    }
  }

  protected void writeEndElement() throws TException {
    try {
      writer().writeEndElement();
    } catch (XMLStreamException e) {
      throw wrap(e);
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

  protected final String expectStartElement(String tagname) throws TException {
    final String actualtag = expectStartElement();
    if (!actualtag.equals(tagname)) {
      throw new IllegalStateException(
        "Expected '" + tagname + "' but was actually '" + actualtag + "'"
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
      throw new IllegalStateException(
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
      throw new TException(e);
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
      throw new TException(
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
      throw new TException(
        "Error reading short attribute '" + localName + "' of '" + el + "'", e
      );
    }
  }

  protected int readIntAttribute(String localName) throws TException {
    try {
      return Integer.valueOf(readAttribute(localName));
    } catch (TException e) {
      throw e;
    } catch (Exception e) {
      String el = reader().hasName() ? reader().getLocalName() : "<unknown>";
      throw new TException(
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
  }

}
