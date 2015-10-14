package org.thriftee.thrift.protocol;

import java.nio.ByteBuffer;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TTransport;
import org.thriftee.thrift.transport.TTransportInputStream;
import org.thriftee.thrift.transport.TTransportOutputStream;

public class TXMLProtocol extends TProtocol {

  public static class Factory implements TProtocolFactory {
    private static final long serialVersionUID = 1017378360734059748L;
    @Override
    public TProtocol getProtocol(TTransport transport) {
      return new TXMLProtocol(transport);
    }
  }

  @Override
  public TField readFieldBegin() throws TException {
    StructContext structCtx = readctx(StructContext.class);
    try {
      final XMLStreamReader r = reader();
      int etype = r.getEventType();
      //for (int etype = r.getEventType(); r.hasNext(); etype = r.next()) {
      if (etype == XMLStreamConstants.CHARACTERS) {
        etype = r.next();
      }
      if (etype == XMLStreamConstants.END_ELEMENT) {
        System.out.print("sending stop field: "); dumpCurrentReadState();
        return new TField();
      }
      if (etype == XMLStreamConstants.START_ELEMENT) {
        FieldContext ctx = new FieldContext(structCtx);
        ctx.name = readLocalName();
        ctx.type = readByteAttribute("type");
        ctx.id = readShortAttribute("fieldId");
        return readnew(ctx).emit();
      }
      throw new IllegalStateException(
        "Unexpected event type: " + XML.streamEventToString(etype));
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }

  @Override
  public void readFieldEnd() throws TException {
    expectEndElement();
    System.out.print("readFieldEnd called: ");
    dumpCurrentReadState();
    readpop(StructContext.class);
  }

  @Override
  public ByteBuffer readBinary() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean readBool() throws TException {
    final String chars = readCharacters();
    return Boolean.valueOf(chars);
  }

  @Override
  public byte readByte() throws TException {
    return Byte.valueOf(readCharacters());
  }

  @Override
  public double readDouble() throws TException {
    return Double.valueOf(readCharacters());
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
  public TList readListBegin() throws TException {
    final FieldContext fieldCtx = readctx(FieldContext.class);
    final ListContext ctx = new ListContext(fieldCtx);
    final String elementName = expectStartElement();
    if (!"list".equals(elementName)) {
      throw new IllegalStateException(
        "Expected 'list' element, but was actually '" + elementName + "'."
      );
    }
    ctx.elemType = readByteAttribute("type");
    ctx.size = readIntAttribute("size");
    try {
      reader().next();
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
    return readnew(ctx).emit();
  }

  @Override
  public void readListEnd() throws TException {
    System.out.println("read list end");
    readpop(FieldContext.class);
  }

  @Override
  public TMap readMapBegin() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readMapEnd() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public TMessage readMessageBegin() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readMessageEnd() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public TSet readSetBegin() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readSetEnd() throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String readString() throws TException {
    return readCharacters();
  }

  @Override
  public TStruct readStructBegin() throws TException {
    final StructContext ctx = new StructContext(readctx(Context.class));
    ctx.name = expectStartElement();
    
    try {
      final XMLStreamReader r = reader();
      r.next();
      /*
      for (int etype = r.getEventType(); r.hasNext(); etype = r.next()) {
        if (etype == XMLStreamConstants.CHARACTERS) {
          continue;
        }
        if (etype == XMLStreamConstants.START_ELEMENT) {
          ctx.name = r.getLocalName();
          r.next();
          break;
        }
        throw new IllegalStateException("unknown readFieldBegin event: " + etype);
      }
      */
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
    return readnew(ctx).emit();
  }

  @Override
  public void readStructEnd() throws TException {
    System.out.println("readStructEnd() called: " + reader().getLocalName());
    dumpCurrentReadState();
    expectEndElement();
    readpop(Context.class);
  }

  @Override
  public void writeBinary(ByteBuffer buffer) throws TException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeBool(boolean bool) throws TException {
    writeCharacters(Boolean.toString(bool));
  }

  @Override
  public void writeByte(byte bite) throws TException {
    // TODO Auto-generated method stub
  }

  @Override
  public void writeDouble(double dbl) throws TException {
    writeCharacters(Double.toString(dbl));
  }

  @Override
  public void writeFieldBegin(TField arg0) throws TException {
    writeStartElement(arg0.name);
    writeAttribute("fieldId", Short.toString(arg0.id));
    writeAttribute("type", Byte.toString(arg0.type));
  }

  @Override
  public void writeFieldEnd() throws TException {
    writeEndElement();
  }

  @Override
  public void writeFieldStop() throws TException {
//    writeStartElement(".stop");
//    writeAttribute("type", Byte.toString(TType.STOP));
//    writeEndElement();
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
  public void writeListBegin(TList list) throws TException {
    writeStartElement("list");
    writeAttribute("type", Byte.toString(list.elemType));
    writeAttribute("size", Integer.toString(list.size));
  }

  @Override
  public void writeListEnd() throws TException {
    writeEndElement();
  }

  @Override
  public void writeMapBegin(TMap map) throws TException {
    writeStartElement("map");
  }

  @Override
  public void writeMapEnd() throws TException {
    writeEndElement();
  }

  @Override
  public void writeMessageBegin(TMessage arg0) throws TException {
    final String msgType;
    switch (arg0.type) {
    case TMessageType.CALL:      msgType = "call";      break;
    case TMessageType.REPLY:     msgType = "reply";     break;
    case TMessageType.EXCEPTION: msgType = "exception"; break;
    case TMessageType.ONEWAY:    msgType = "oneway";    break;
    default: throw new IllegalStateException("unknown msg type: " + arg0.type);
    }
    try {
      writer().writeStartElement(msgType);
    } catch (XMLStreamException e) {
      throw wrap(e);
    }
  }

  @Override
  public void writeMessageEnd() throws TException {
    writeEndElement();
  }

  @Override
  public void writeSetBegin(TSet set) throws TException {
    writeStartElement("set");
  }

  @Override
  public void writeSetEnd() throws TException {
    writeEndElement();
  }

  @Override
  public void writeString(String str) throws TException {
    writeCharacters(str);
  }

  @Override
  public void writeStructBegin(TStruct arg0) throws TException {
    writeStartElement(arg0.name);
  }

  @Override
  public void writeStructEnd() throws TException {
    writeEndElement();
  }

  class Context {
    final Context parent;
    Context(Context parent) {
      this.parent = parent;
    }
  }

  class MessageContext extends Context {
    MessageContext(Context parent) {
      super(parent);
    }
  }

  class StructContext extends Context {
    String name;
    StructContext(Context parent) {
      super(parent);
    }
    void read() throws TException {
      try {
        final XMLStreamReader r = reader();
        for (int etype = r.getEventType(); r.hasNext(); etype = r.next()) {
          if (etype == XMLStreamConstants.CHARACTERS) {
            continue;
          }
          if (etype == XMLStreamConstants.START_ELEMENT) {
            break;
          }
          throw new IllegalStateException("unknown event: " + etype);
        }
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
      name = reader().getLocalName();
    }
    TStruct emit() {
      return new TStruct(name);
    }
    public String toString() {
      return "<TStruct name:'" + name + "'>";
    }
  }

  class FieldContext extends Context {
    String name;
    byte type;
    short id;
    FieldContext(StructContext struct) {
      super(struct);
      if (struct == null) {
        throw new IllegalArgumentException("parent struct cannot be null.");
      }
    }
    StructContext struct() {
      return (StructContext) parent;
    }
    TField emit() {
      return new TField(name, type, id);
    }
    public String toString() {
      return emit().toString();
    }
  }

  class ListContext extends Context {
    byte elemType;
    int size;
    ListContext(FieldContext field) {
      super(field);
      if (field == null) {
        throw new IllegalArgumentException("parent field cannot be null.");
      }
    }
    FieldContext field() {
      return (FieldContext) parent;
    }
    TList emit() {
      return new TList(elemType, size);
    }
    public String toString() {
      return "<TList type:" + elemType + " size:" + size + ">";
    }
  }

  protected Context readctx() {
    return readContext;
  }

  protected static <T extends Context> T current(Class<T> type, Context ctx) {
    if (type.isAssignableFrom(ctx.getClass())) {
      return type.cast(ctx);
    }
    throw new IllegalArgumentException(
      "Expected " + type.getSimpleName() + 
      " but was actually " + ctx.getClass().getSimpleName()
    );
  }

  protected <T extends Context> T readctx(Class<T> type) {
    return current(type, readctx());
  }

  protected <T extends Context> T readnew(T newctx) {
    for (Context top = newctx.parent; top != null; top = top.parent) {
      System.out.print("  ");
    }
    System.out.println("new: " + newctx);
    this.readContext = newctx;
    return newctx;
  }

  protected <T extends Context> T readpop(Class<T> oldtype) {
    for (Context top = readctx().parent; top != null; top = top.parent) {
      System.out.print("  ");
    }
    System.out.println("pop: " + readctx());
    T oldctx = current(oldtype, readctx().parent);
    this.readContext = oldctx;
    return oldctx;
  }

  public TXMLProtocol(TTransport trans) {
    super(trans);
  }

  protected XMLStreamReader reader() throws TException {
    if (__reader == null) {
      try {
        __reader = DEFAULT_XML_IN.createFilteredReader(
          DEFAULT_XML_IN.createXMLStreamReader(
            new TTransportInputStream(getTransport())
          )
          , new TXMLProtocolStreamFilter()
        );
      } catch (XMLStreamException e) {
        throw new TException(e);
      }
    }
    return __reader;
  }

  protected String expectStartElement() throws TException {
    try {
      final XMLStreamReader r = reader();
      dumpCurrentReadState();
      if (r.getEventType() == XMLStreamConstants.CHARACTERS) {
        r.next();
        dumpCurrentReadState();
      }
      if (r.getEventType() == XMLStreamConstants.START_ELEMENT) {
        return r.getLocalName();
      }
      throw new IllegalStateException(
          "unknown startElement event: " + r.getEventType());
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }
  
  protected void expectEndElement() throws TException {
    try {
      final XMLStreamReader r = reader();
      int etype = r.getEventType();
      if (etype == XMLStreamConstants.CHARACTERS) {
        etype = r.next();
      }
      if (etype == XMLStreamConstants.END_ELEMENT) {
        if (r.hasNext()) {
          r.next();
        }
      } else {
        throw new IllegalStateException("unknown endElement event: " + etype);
      }
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }

  protected void consumeReader() throws TException {
    try {
      final XMLStreamReader r = reader();
      for ( ; r.hasNext(); r.next()) {
        dumpCurrentReadState();
      }
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }

  /*
  protected void nextStartElement() throws TException {
    try {
      for (final XMLStreamReader reader = reader(); reader.hasNext(); ) {
        final int eventType = reader.next();
        if (eventType == XMLStreamConstants.START_ELEMENT) {
          return;
        }
      }
      throw new IllegalStateException(
        "Reached end of parsing without encountering START_ELEMENT"
      );
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }


  protected boolean hasEndElementBeforeNextStartElement() throws TException {
    boolean result = false;
    try {
      int eventType = reader().next();
      if (eventType == XMLStreamConstants.CHARACTERS) {
        eventType = reader().next();
      }
      if (eventType == XMLStreamConstants.START_ELEMENT) {
        return false;
      } else if (eventType == XMLStreamConstants.END_ELEMENT) {
        return true;
      } else {
        throw new IllegalStateException("unknown event type: " + eventType);
      }
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }
*/

  protected XMLStreamWriter writer() throws XMLStreamException {
    if (__writer == null) {
      __writer = DEFAULT_XML_OUT.createXMLStreamWriter(
        new TTransportOutputStream(getTransport())
      );
    }
    return __writer;
  }
  
  protected String readLocalName() throws TException {
    return reader().getLocalName();
    /*
    try {
      for (final XMLStreamReader reader = reader(); reader.hasNext(); ) {
        final int eventType = reader.next();
        if (eventType == XMLStreamConstants.START_ELEMENT) {
          return reader.getLocalName();
        }
      }
      throw new IllegalStateException(
        "Reached end of parsing without encountering START_ELEMENT"
      );
    } catch (XMLStreamException e) {
      throw wrap(e);
    }
    */
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
      if (etype == XMLStreamConstants.START_ELEMENT) {
        etype = reader().next();
      }
      if (etype == XMLStreamConstants.CHARACTERS) {
        return reader().getText();
      }
      throw new TException(
        "expected CHARACTERS but was " + XML.streamEventToString(etype));
    } catch (XMLStreamException e) {
      throw new TException(e);
    }
  }

  private static final XMLInputFactory DEFAULT_XML_IN = XMLInputFactory.newFactory();
  static {
    DEFAULT_XML_IN.setProperty(XMLInputFactory.IS_COALESCING, true);
  }

  private static final XMLOutputFactory DEFAULT_XML_OUT = XMLOutputFactory.newFactory();

  private XMLStreamReader __reader;

  private XMLStreamWriter __writer;

  private Context readContext = new Context(null);

  private Context writeContext = new Context(null);

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

  protected void writeCharacters(String s) throws TException {
    try {
      writer().writeCharacters(s);
    } catch (XMLStreamException e) {
      throw wrap(e);
    }
  }

  private class TXMLProtocolStreamFilter implements StreamFilter {
    @Override
    public boolean accept(XMLStreamReader reader) {
      switch (reader.getEventType()) {
      case XMLStreamConstants.START_ELEMENT:
      case XMLStreamConstants.END_ELEMENT:
      case XMLStreamConstants.ATTRIBUTE:
      case XMLStreamConstants.CHARACTERS:
      case XMLStreamConstants.END_DOCUMENT:
        return true;
      default:
        return false;
      }
    }
  }

  protected void dumpCurrentReadState() throws TException {
    System.out.println(XML.dumpCurrentState(reader()));
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
