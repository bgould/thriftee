package org.thriftee.thrift.protocol;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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

public class TXMLProtocol extends AbstractContextProtocol {

  public static enum Verbosity {

    CONCISE(
      "http://thrift.apache.org/xml/protocol/concise",
      "s", "f", "m", "_", "e", "e", "k", "v", 
      "t", "ct", "kt", "vt", "sz", "n", "i", "q", false
    ),
    VERBOSE(
      "http://thrift.apache.org/xml/protocol/verbose",
      "struct", 
      "field", 
      "message", 
      "stop", 
      "item", 
      "entry", 
      "key", 
      "value",
      "type",
      "containertype",
      "keytype",
      "valuetype",
      "size",
      "name",
      "id",
      "seqid",
      true
    ),
    ;

    public final String NAMESPACE;
    public final String ELEMENT_STRUCT;
    public final String ELEMENT_FIELD;
    public final String ELEMENT_MESSAGE;
    public final String ELEMENT_STOP;
    public final String ELEMENT_ITEM;
    public final String ELEMENT_ENTRY;
    public final String ELEMENT_KEY;
    public final String ELEMENT_VALUE;
    public final String ATTRIBUTE_TYPE;
    public final String ATTRIBUTE_CONTAINER_TYPE;
    public final String ATTRIBUTE_KEY_TYPE;
    public final String ATTRIBUTE_VALUE_TYPE;
    public final String ATTRIBUTE_SIZE;
    public final String ATTRIBUTE_NAME;
    public final String ATTRIBUTE_ID;
    public final String ATTRIBUTE_SEQID;
    public final boolean INCLUDE_NAMES;
    private Verbosity(
        String namespace,
        String structElement, 
        String fieldElement, 
        String messageElement, 
        String stopElement,
        String itemElement, 
        String entryElement, 
        String keyElement, 
        String valueElement,
        String typeAttribute,
        String containerTypeAttribute,
        String keyTypeAttribute,
        String valueTypeAttribute,
        String sizeAttribute,
        String nameAttribute,
        String idAttribute,
        String seqidAttribute,
        boolean includeNames) {
      NAMESPACE = namespace;
      ELEMENT_STRUCT = structElement;
      ELEMENT_FIELD = fieldElement;
      ELEMENT_MESSAGE = messageElement;
      ELEMENT_STOP = stopElement;
      ELEMENT_ITEM = itemElement;
      ELEMENT_ENTRY = entryElement;
      ELEMENT_KEY = keyElement;
      ELEMENT_VALUE = valueElement;
      INCLUDE_NAMES = includeNames;
      ATTRIBUTE_TYPE = typeAttribute;
      ATTRIBUTE_CONTAINER_TYPE = containerTypeAttribute;
      ATTRIBUTE_KEY_TYPE = keyTypeAttribute;
      ATTRIBUTE_VALUE_TYPE = valueTypeAttribute;
      ATTRIBUTE_SIZE = sizeAttribute;
      ATTRIBUTE_NAME = nameAttribute;
      ATTRIBUTE_ID = idAttribute;
      ATTRIBUTE_SEQID = seqidAttribute;
    }

  }

  public static class Factory implements TProtocolFactory {

    private static final long serialVersionUID = 1017378360734059748L;

    private final Verbosity variant;

    public Factory() {
      this(null);
    }

    public Factory(Verbosity variant) {
      if (variant == null) {
        variant = Verbosity.CONCISE;
      }
      this.variant = variant;
    }

    @Override
    public TProtocol getProtocol(TTransport transport) {
      return new TXMLProtocol(transport, variant);
    }

  }

  private final Verbosity variant;

  public TXMLProtocol(TTransport trans, Verbosity variant) {
    super(trans);
    this.variant = variant;
  }

  public Verbosity variant() {
    return this.variant;
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
      return readCharacters();
    }

    @Override
    public byte readByte() throws TException {
      return Byte.valueOf(readCharacters());
    }

    @Override
    public short readI16() throws TException {
      return Short.valueOf(readCharacters());
    }

    @Override
    public int readI32() throws TException {
      return Integer.valueOf(readCharacters());
    }

    @Override
    public long readI64() throws TException {
      return Long.valueOf(readCharacters());
    }

    @Override
    public double readDouble() throws TException {
      return Double.valueOf(readCharacters());
    }

    @Override
    public ByteBuffer readBinary() throws TException {
      return ByteBuffer.wrap(Base64.decodeBase64(readCharacters()));
    }

    @Override
    public boolean readBool() throws TException {
      return Boolean.valueOf(readCharacters());
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
      writeStartElement(variant().ELEMENT_MESSAGE);
      writeAttribute(variant().ATTRIBUTE_NAME, name);
      writeAttribute(variant().ATTRIBUTE_TYPE, Byte.toString(type));
      writeAttribute(variant().ATTRIBUTE_SEQID, Integer.toString(seqid));
      return this;
    }

    @Override
    public XMLMessageContext readStart() throws TException {
      nextStartElement();
      this.name = readAttribute(variant().ATTRIBUTE_NAME);
      this.type = readByteAttribute(variant().ATTRIBUTE_TYPE);
      this.seqid = readIntAttribute(variant().ATTRIBUTE_SEQID);
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
      writeStartElement(variant().ELEMENT_STRUCT);
      return this;
    }

    @Override
    public StructContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public StructContext writeFieldStop() throws TException {
      //writeStartElement("_.");
      writeStartElement(variant().ELEMENT_STOP);
      writeEndElement();
      return this;
    }

    @Override 
    public XMLStructContext readStart() throws TException {
      if (variant().INCLUDE_NAMES) {
        this.name = nextStartElement();
      } else {
        nextStartElement();
      }
      return this;
    }

    @Override 
    public XMLStructContext readEnd() throws TException {
      nextEndElement();
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
      writeStartElement(variant().ELEMENT_FIELD);
      if (variant().INCLUDE_NAMES) {
        writeAttribute(variant().ATTRIBUTE_NAME, name);
      }
      writeAttribute(variant().ATTRIBUTE_ID, Short.toString(id));
      writeAttribute(variant().ATTRIBUTE_TYPE, Byte.toString(type));
      return this;
    }

    @Override
    public XMLFieldContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public XMLFieldContext readStart() throws TException {
      final String name = nextStartElement();
      if (variant().ELEMENT_STOP.equals(name)) {
        this.type = 0;
        this.id = 0;
      } else {
        this.id = readShortAttribute(variant().ATTRIBUTE_ID);
        this.type = readByteAttribute(variant().ATTRIBUTE_TYPE);
      }
      return this;
    }

    @Override
    public XMLFieldContext readEnd() throws TException {
      //nextEndElement(this.name);
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
      writeAttribute(variant().ATTRIBUTE_CONTAINER_TYPE, containerType().name().toLowerCase());
      writeAttribute("csize", Integer.toString(size));
      writeAttribute(variant().ATTRIBUTE_VALUE_TYPE, Byte.toString(elemType));
      return this;
    }

    @Override 
    public ContainerContext<T> writeEnd() throws TException {
      return this;
    }

    @Override 
    public ContainerContext<T> readStart() throws TException {
      final int etype = reader().getEventType();
      final String containerType = containerType().strval();
      if (etype == START_ELEMENT) {
        final String ctype = readAttribute(variant().ATTRIBUTE_CONTAINER_TYPE);
        if (!(containerType.equals(ctype))) {
          throw new IllegalStateException(
            "Expected '" + containerType + "' but was '" + ctype + "'");
        }
        this.size = readIntAttribute("csize");
        this.elemType = readByteAttribute(variant().ATTRIBUTE_VALUE_TYPE);
        if ("map".equals(ctype)) {
          ((XMLMapContext)this).keyType = readByteAttribute(variant().ATTRIBUTE_KEY_TYPE);
        }
      } else {
        throw new IllegalStateException(
          "Expected START_ELEMENT but was " + XML.streamEventToString(etype)
        );
      }
      return this;
    }

    @Override
    public ContainerContext<T> readEnd() throws TException {
      return this;
    }

  }

  public class XMLListContext 
      extends XMLContainerContext<TList> implements ListContext {

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

    @Override
    protected void writeCharacters(String chars) throws TException {
      writeStartElement(variant().ELEMENT_ITEM);
      super.writeCharacters(chars);
      writeEndElement();
    }

    @Override
    protected String readCharacters() throws TException {
      nextStartElement(variant().ELEMENT_ITEM);
      final String result = super.readCharacters();
      readerNext();
      return result;
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

    @Override
    protected void writeCharacters(String chars) throws TException {
      writeStartElement(variant().ELEMENT_ITEM);
      super.writeCharacters(chars);
      writeEndElement();
    }

    @Override
    protected String readCharacters() throws TException {
      nextStartElement(variant().ELEMENT_ITEM);
      final String result = super.readCharacters();
      readerNext();
      return result;
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
      writeAttribute(variant().ATTRIBUTE_KEY_TYPE, Byte.toString(keyType));
      return this;
    }

    @Override
    public void writeCharacters(String s) throws TException {
      final boolean isKey = childCount % 2 == 0;
      if (isKey) {
        writeStartElement(variant().ELEMENT_ENTRY);
        writeStartElement(variant().ELEMENT_KEY);
        super.writeCharacters(s);
        writeEndElement(); // key
      } else {
        writeStartElement(variant().ELEMENT_VALUE);
        super.writeCharacters(s);
        writeEndElement(); // value
        writeEndElement(); // entry
      }
      childCount++;
    }

    @Override
    public String readCharacters() throws TException {
      final boolean isKey = childCount % 2 == 0;
      final String result;
      if (isKey) {
        nextStartElement(variant().ELEMENT_ENTRY);
        nextStartElement(variant().ELEMENT_KEY);
        result = super.readCharacters();
        nextEndElement(); // end element key
      } else {
        nextStartElement(variant().ELEMENT_VALUE);
        result = super.readCharacters();
        nextEndElement(); // end element value
        nextEndElement(); // end element entry
      }
      childCount++;
      return result;
    }

    @Override
    public void pushed() throws TException {
      final boolean isKey = childCount % 2 == 0;
      switch (type()) {
        case READ:
          if (isKey) {
            nextStartElement(variant().ELEMENT_ENTRY);
            nextStartElement(variant().ELEMENT_KEY);
          } else {
            nextStartElement(variant().ELEMENT_VALUE);
          }
          break;
        case WRITE:
          if (isKey) {
            writeStartElement(variant().ELEMENT_ENTRY);
            writeStartElement(variant().ELEMENT_KEY);
          } else {
            writeStartElement(variant().ELEMENT_VALUE);
          }
          break;
      }
    }

    @Override
    public void popped() throws TException {
      final boolean isKey = childCount % 2 == 0;
      switch (type()) {
      case READ:
        if (isKey) {
          nextEndElement(variant().ELEMENT_KEY);
        } else {
          nextEndElement(variant().ELEMENT_VALUE);
          nextEndElement(variant().ELEMENT_ENTRY);
        }
        break;
      case WRITE:
        if (isKey) {
          writeEndElement();
        } else {
          writeEndElement();
          writeEndElement();
        }
        break;
      }
      childCount++;
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
        __reader = xmlInputFactory().createXMLStreamReader(
          new TTransportInputStream(getTransport())
        );
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
