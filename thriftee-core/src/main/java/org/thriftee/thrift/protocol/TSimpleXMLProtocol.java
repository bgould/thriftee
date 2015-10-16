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

public class TSimpleXMLProtocol extends AbstractXMLProtocol {

  public static class Factory implements TProtocolFactory {
    private static final long serialVersionUID = 1017378360734059748L;
    @Override
    public TProtocol getProtocol(TTransport transport) {
      return new TSimpleXMLProtocol(transport);
    }
  }

  public abstract class StreamingValueHolderContext 
      extends AbstractContext 
      implements ValueHolderContext {

    public StreamingValueHolderContext(Context context) {
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

    @Override 
    public SimpleListContext newList() throws TException {
      return new SimpleListContext(this);
    }

    @Override 
    public SimpleSetContext newSet() throws TException {
      return new SimpleSetContext(this);
    }

    @Override 
    public SimpleMapContext newMap() throws TException {
      return new SimpleMapContext(this);
    }

    @Override
    public SimpleStructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }

  }

  public class SimpleBaseContext extends BaseContext {

    public SimpleBaseContext(ContextType type) {
      super(type);
    }

    @Override 
    public MessageContext newMessage() throws TException {
      return new SimpleMessageContext(this);
    }

    @Override public StructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }

  }

  class SimpleMessageContext extends AbstractContext implements MessageContext {
    public SimpleMessageContext(Context parent) {
      super(parent);
    }
    public SimpleMessageContext writeStart() throws TException { throw up(); }
    public SimpleMessageContext readStart()  throws TException { throw up(); }
    public SimpleMessageContext writeEnd()   throws TException { throw up(); }
    public SimpleMessageContext readEnd()    throws TException { throw up(); }
    public SimpleStructContext  newStruct()  throws TException { throw up(); }
    public TMessage emit() { throw up(); }
    public void read(TMessage msg) { throw up(); }
  }

  public class SimpleStructContext 
        extends AbstractStructContext 
        implements StructContext {

    public SimpleStructContext(Context parent) {
      super(parent);
    }

    @Override 
    public StructContext writeStart() throws TException {
      writeStartElement(name);
      return this;
    }

    @Override
    public StructContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public StructContext writeFieldStop() throws TException {
      writeStartElement("_.");
      writeEndElement();
      return this;
    }

    @Override 
    public SimpleStructContext readStart() throws TException {
      try {
        int eventType = reader().next();
        if (eventType == CHARACTERS) {
          eventType = reader().next();
        }
        if (eventType == START_ELEMENT) {
          this.name = reader().getLocalName();
          return this;
        } else {
          throw new IllegalStateException();
        }
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }

    @Override 
    public SimpleStructContext readEnd() throws TException {
      try {
        int eventType = reader().next();
        if (eventType == CHARACTERS) {
          eventType = reader().next();
        }
        if (eventType == END_ELEMENT) {
          if (!this.name.equals(reader().getLocalName())) {
            throw new IllegalStateException();
          }
        }
        return this;
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }

    @Override 
    public SimpleFieldContext newField() throws TException {
      return new SimpleFieldContext(this);
    }

  }

  public class SimpleFieldContext extends StreamingValueHolderContext implements FieldContext {

    String name;
    byte type;
    short id;

    public SimpleFieldContext(StructContext struct) {
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
    public SimpleFieldContext writeStart() throws TException {
      writeStartElement(name);
      writeAttribute("i", Short.toString(id));
      writeAttribute("type", Byte.toString(type));
      return this;
    }

    @Override
    public SimpleFieldContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }

    @Override
    public SimpleFieldContext readStart() throws TException {
      try {
        int eventType = reader().next();
        if (eventType == CHARACTERS) {
          eventType = reader().next();
        }
        if (eventType == START_ELEMENT) {
          this.name = reader().getLocalName();
          if ("_.".equals(this.name)) {
            this.type = 0;
            this.id = 0;
          } else {
            this.id = readShortAttribute("i");
            this.type = readByteAttribute("type");
          }
          return this;
        } else {
          throw new IllegalStateException();
        }
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }

    @Override
    public SimpleFieldContext readEnd() throws TException {
      try {
        int eventType = reader().next();
        if (eventType == CHARACTERS) {
          eventType = reader().next();
        }
        if (eventType == END_ELEMENT) {
          if (!this.name.equals(reader().getLocalName())) {
            throw new IllegalStateException();
          }
        }
        return this;
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }

  }

  public class SimpleListContext extends AbstractListContext {

    public SimpleListContext(ValueHolderContext field) {
      super(field);
    }

    @Override public SimpleListContext readStart() throws TException {
      readContainerStart(this);
      return this;
    }

    @Override public SimpleListContext readEnd() throws TException {
      readContainerEnd(this);
      return this;
    }

    @Override
    public void read(TList obj) {
      this.elemType = obj.elemType;
      this.size = obj.size;
    }

  }

  class SimpleSetContext extends AbstractSetContext {
    SimpleSetContext(ValueHolderContext field) {
      super(field);
    }
    @Override
    public SimpleSetContext readStart() throws TException {
      readContainerStart(this);
      return this;
    }
    @Override
    public SimpleSetContext readEnd() throws TException {
      readContainerEnd(this);
      return this;
    }
  }

  class SimpleMapContext extends AbstractMapContext {
    SimpleMapContext(ValueHolderContext field) {
      super(field);
    }
    @Override
    public SimpleMapContext readStart() throws TException {
      readContainerStart(this);
      return this;
    }
    @Override
    public SimpleMapContext readEnd() throws TException {
      readContainerEnd(this);
      return this;
    }
    @Override
    public void read(TMap obj) {
      this.elemType = obj.valueType;
      this.keyType = obj.keyType;
      this.size = obj.size;
    }
  }

  private void readContainerStart(AbstractContainerContext<?> ctx) throws TException {
      final int etype = reader().getEventType();
      final String containerType = ctx.containerType().toString().toLowerCase();
      if (etype == START_ELEMENT) {
        final String ctype = readAttribute("ctype");
        if (!(ctx.containerType().equals(ctype))) {
          throw new IllegalStateException(
            "Expected '" + containerType + "' but was '" + ctype + "'");
        }
        ctx.size = readIntAttribute("csize");
        ctx.elemType = readByteAttribute("vtype");
        if ("map".equals(ctype)) {
          ((AbstractMapContext)ctx).keyType = readByteAttribute("ktype");
        }
      } else {
        throw new IllegalStateException(
          "Expected START_ELEMENT but was " + XML.streamEventToString(etype)
        );
      }
    }

  private void readContainerEnd(ContainerContext<?> ctx) throws TException {

  }

  public TSimpleXMLProtocol(TTransport trans) {
    super(trans);
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

  private static final XMLInputFactory XML_IN = XMLInputFactory.newFactory();
  static {
    XML_IN.setProperty(XMLInputFactory.IS_COALESCING, true);
  }

  protected XMLInputFactory xmlInputFactory() {
    return XML_IN;
  }

  private static final XMLOutputFactory XML_OUT = XMLOutputFactory.newFactory();

  protected XMLOutputFactory xmlOutputFactory() {
    return XML_OUT;
  }

  @Override
  protected BaseContext createBaseContext(ContextType type) {
    return new SimpleBaseContext(type);
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

  abstract class AbstractFieldContext 
        extends StreamingValueHolderContext implements FieldContext {
    String name;
    byte type;
    short id;
    AbstractFieldContext(StructContext struct) {
      super(struct);
      if (struct == null) {
        throw new IllegalArgumentException("parent struct cannot be null.");
      }
    }
    public TField emit() {
      return new TField(name, type, id);
    }
    public String toString() {
      return emit().toString();
    }
    @Override public FieldContext writeStart() throws TException {
      writeStartElement(name);
      writeAttribute("i", Short.toString(id));
      writeAttribute("type", Byte.toString(type));
      return this;
    }
    @Override public FieldContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }
  }

  abstract class AbstractContainerContext<T> 
      extends StreamingValueHolderContext implements ContainerContext<T> {
    byte elemType;
    int size;
    final Class<T> emitType;
    final ContainerType containerType;
    protected AbstractContainerContext(
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
    public ContainerType containerType() {
      return containerType;
    }
    @Override 
    public ContainerContext<T> writeStart() throws TException {
      writeAttribute("ctype", containerType().name().toLowerCase());
      writeAttribute("csize", Integer.toString(size));
      writeAttribute("vtype", Byte.toString(elemType));
      return this;
    }
    @Override 
    public ContainerContext<T> writeEnd() throws TException {
      //writeEndElement();
      return this;
    }
  }

  abstract class AbstractListContext extends AbstractContainerContext<TList> implements ListContext {
    AbstractListContext(ValueHolderContext field) {
      super(field, TList.class, ContainerType.LIST);
    }
    @Override
    public TList emit() {
      return new TList(elemType, size);
    }
    @Override protected void writeCharacters(String chars) throws TException {
      writeStartElement("item");
      super.writeCharacters(chars);
      writeEndElement();
    }
  }

  public abstract class AbstractSetContext extends AbstractContainerContext<TSet> implements SetContext {
    AbstractSetContext(ValueHolderContext field) {
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
      writeStartElement("item");
      super.writeCharacters(chars);
      writeEndElement();
    }
    
  }

  abstract class AbstractMapContext extends AbstractContainerContext<TMap> implements MapContext {
    byte keyType;
    AbstractMapContext(ValueHolderContext field) {
      super(field, TMap.class, ContainerType.MAP);
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
      writeAttribute("ktype", Byte.toString(keyType));
      return this;
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
