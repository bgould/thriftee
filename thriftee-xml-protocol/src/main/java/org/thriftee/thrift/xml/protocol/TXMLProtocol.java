package org.thriftee.thrift.xml.protocol;

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
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
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
import org.thriftee.thrift.xml.transport.TTransportInputStream;
import org.thriftee.thrift.xml.transport.TTransportOutputStream;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TXMLProtocol extends AbstractContextProtocol {

  private static final String[] VERBOSE_MESSAGE_NAMES = new String[5];
  static {
    VERBOSE_MESSAGE_NAMES[CALL]      = "call";
    VERBOSE_MESSAGE_NAMES[REPLY]     = "reply";
    VERBOSE_MESSAGE_NAMES[EXCEPTION] = "exception";
    VERBOSE_MESSAGE_NAMES[ONEWAY]    = "oneway";
  }

  private static final String[] VERBOSE_TYPE_NAMES = new String[17];
  static {
    VERBOSE_TYPE_NAMES[STOP]   = "stop";
    VERBOSE_TYPE_NAMES[VOID]   = "void";
    VERBOSE_TYPE_NAMES[BOOL]   = "bool";
    VERBOSE_TYPE_NAMES[BYTE]   = "i8";
    VERBOSE_TYPE_NAMES[DOUBLE] = "double";
    VERBOSE_TYPE_NAMES[I16]    = "i16";
    VERBOSE_TYPE_NAMES[I32]    = "i32";
    VERBOSE_TYPE_NAMES[I64]    = "i64";
    VERBOSE_TYPE_NAMES[STRING] = "string";
    VERBOSE_TYPE_NAMES[STRUCT] = "struct";
    VERBOSE_TYPE_NAMES[MAP]    = "map";
    VERBOSE_TYPE_NAMES[SET]    = "set";
    VERBOSE_TYPE_NAMES[LIST]   = "list";
    VERBOSE_TYPE_NAMES[ENUM]   = "enum";
  }

  // TODO: this could probably be made a little faster
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
      "http://thrift.apache.org/xml/protocol", 
      "k", 
      "v", 
      "t", 
      "z", 
      "n", 
      "i",
      null,
      "q", 
      false,
      new NameConverter() {
        public byte elementToByte(String s) {
          return Byte.parseByte(s.substring(1));
        }
        public String byteToElement(byte b) {
          return "t" + b;
        }
        public byte messageTypeToByte(String s) {
          return Byte.parseByte(s.substring(1));
        }
        public String byteToMessageType(byte b) {
          return "m" + b;
        }
      }
    ),
    VERBOSE(
      "http://thrift.apache.org/xml/protocol",
      "key",
      "value",
      "type",
      "size",
      "name",
      "field",
      "fname",
      "seqid",
      true,
      new NameConverter() {
        public byte elementToByte(String s) {
          return search(VERBOSE_TYPE_NAMES, s);
        }
        public String byteToElement(byte b) {
          return VERBOSE_TYPE_NAMES[b];
        }
        public byte messageTypeToByte(String s) {
          return search(VERBOSE_MESSAGE_NAMES, s);
        }
        public String byteToMessageType(byte b) {
          return VERBOSE_MESSAGE_NAMES[b];
        }
      }
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
    public final NameConverter converter;
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
        NameConverter converter) {
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
      this.converter = converter;
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
        throw new TException(e);
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
    public void writeBinary(ByteBuffer buffer) throws TException {
      writeCharacters(DatatypeConverter.printBase64Binary(buffer.array()));
    }

    @Override
    public void writeBool(boolean bool) throws TException {
      writeCharacters(DatatypeConverter.printBoolean(bool));
    }

    @Override
    public void writeByte(byte bite) throws TException {
      writeCharacters(DatatypeConverter.printByte(bite));
    }

    @Override
    public void writeDouble(double dbl) throws TException {
      writeCharacters(DatatypeConverter.printDouble(dbl));
    }

    @Override
    public void writeI16(short i16) throws TException {
      writeCharacters(DatatypeConverter.printShort(i16));
    }

    @Override
    public void writeI32(int i32) throws TException {
      writeCharacters(DatatypeConverter.printInt(i32));
    }

    @Override
    public void writeI64(long i64) throws TException {
      writeCharacters(DatatypeConverter.printLong(i64));
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
      final byte result = DatatypeConverter.parseByte(readCharacters());
      return result;
    }

    @Override
    public short readI16() throws TException {
      final short result = DatatypeConverter.parseShort(readCharacters());
      return result;
    }

    @Override
    public int readI32() throws TException {
      final int result = DatatypeConverter.parseInt(readCharacters());
      return result;
    }

    @Override
    public long readI64() throws TException {
      final long result = DatatypeConverter.parseLong(readCharacters());
      return result;
    }

    @Override
    public double readDouble() throws TException {
      final double result = DatatypeConverter.parseDouble(readCharacters());
      return result;
    }

    @Override
    public ByteBuffer readBinary() throws TException {
      final byte[] val = DatatypeConverter.parseBase64Binary(readCharacters());
      return ByteBuffer.wrap(val);
    }

    @Override
    public boolean readBool() throws TException {
      final boolean result = DatatypeConverter.parseBoolean(readCharacters());
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

    @Override
    public void popped() throws TException {
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
    public XMLMessageContext writeStart() throws TException {
      writeStartElement(byteToMessageType(type));
      writeAttribute("xmlns", variant.NAMESPACE);
      writeAttribute(variant.ATTRIBUTE_NAME, name);
      writeAttribute(variant.ATTRIBUTE_SEQID, Integer.toString(seqid));
      return this;
    }

    @Override
    public XMLMessageContext readStart() throws TException {
      final String msgname = nextStartElement();
      this.type = messageTypeToByte(msgname);
      this.name = readAttribute(variant.ATTRIBUTE_NAME);
      // TODO: seqid should always be required
      if (reader().getAttributeValue(null, variant.ATTRIBUTE_SEQID) != null) {
        this.seqid = readIntAttribute(variant.ATTRIBUTE_SEQID);
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
      int eventType = readerNext();
      if (eventType == CHARACTERS) {
        eventType = readerNext();
      }
      if (eventType == START_ELEMENT) {
        this.type = elementToByte(reader().getLocalName());
        this.id = readShortAttribute(variant.ATTRIBUTE_ID);
        if (variant.ATTRIBUTE_FIELD_NAME != null) {
          name = readAttribute(variant.ATTRIBUTE_FIELD_NAME);
        }
      } else {
        this.type = STOP;
        this.id = 0;
      }
      return this;
    }

    @Override
    public XMLFieldContext readEnd() throws TException {
      // stop fields don't exist, so can't read the end element
      // structs should already have had their end element as their stop field
      if (this.type != STOP && this.type != STRUCT) {
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
      writeAttribute(variant.ATTRIBUTE_SIZE, Integer.toString(size));
      writeAttribute(variant.ATTRIBUTE_VALUE_TYPE, byteToElement(elemType));
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
      if (parent() instanceof ContainerContext<?>) {
        nextEndElement();
      }
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
    return variant.converter.elementToByte(element);
  }

  protected String byteToElement(byte type) {
    return variant.converter.byteToElement(type);
  }

  protected byte messageTypeToByte(String element) {
    return variant.converter.messageTypeToByte(element);
  }

  protected String byteToMessageType(byte type) {
    return variant.converter.byteToMessageType(type);
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

  interface NameConverter {
    byte elementToByte(String s);
    String byteToElement(byte b);
    byte messageTypeToByte(String s);
    String byteToMessageType(byte b);
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
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
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
    public static String transform(
          final URL xsltUrl, 
          final Source source, 
          final Map<String, Object> params
        ) throws IOException, TransformerException {
      try {
        final TransformerFactory tf = TransformerFactory.newInstance();
        final StreamSource xsltSource = new StreamSource(xsltUrl.openStream());
        final Transformer transformer = tf.newTransformer(xsltSource);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        if (params != null) {
          for (Entry<String, Object> entry : params.entrySet()) {
            transformer.setParameter(entry.getKey(), entry.getValue());
          }
        }
        final StringWriter sw = new StringWriter();
        transformer.transform(source, new StreamResult(sw));
        return sw.toString();
      } catch (TransformerConfigurationException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
