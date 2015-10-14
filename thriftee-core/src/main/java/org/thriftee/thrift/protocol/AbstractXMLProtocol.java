package org.thriftee.thrift.protocol;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.thrift.transport.TTransportOutputStream;

public abstract class AbstractXMLProtocol extends TProtocol {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

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
  public void writeStructBegin(TStruct struct) throws TException {
    final StructContext ctx = writectx.peek().newStruct();
    ctx.name = struct.name;
    ctx.writeStart();
  }

  @Override
  public void writeStructEnd() throws TException {
    writectx.peek().writeEnd();
  }
  
  @Override
  public ByteBuffer readBinary() throws TException {
    throw up();
  }

  @Override
  public boolean readBool() throws TException {
    throw up();
  }

  @Override
  public byte readByte() throws TException {
    throw up();
  }

  @Override
  public double readDouble() throws TException {
    throw up();
  }

  @Override
  public TField readFieldBegin() throws TException {
    throw up();
  }

  @Override
  public void readFieldEnd() throws TException {
    throw up();
  }

  @Override
  public short readI16() throws TException {
    throw up();
  }

  @Override
  public int readI32() throws TException {
    throw up();
  }

  @Override
  public long readI64() throws TException {
    throw up();
  }

  @Override
  public TList readListBegin() throws TException {
    throw up();
  }

  @Override
  public void readListEnd() throws TException {
    throw up();
  }

  @Override
  public TMap readMapBegin() throws TException {
    throw up();
  }

  @Override
  public void readMapEnd() throws TException {
    throw up();
  }

  @Override
  public TMessage readMessageBegin() throws TException {
    throw up();
  }

  @Override
  public void readMessageEnd() throws TException {
    throw up();
  }

  @Override
  public TSet readSetBegin() throws TException {
    throw up();
  }

  @Override
  public void readSetEnd() throws TException {
    throw up();
  }

  @Override
  public String readString() throws TException {
    throw up();
  }

  @Override
  public TStruct readStructBegin() throws TException {
    throw up();
  }

  @Override
  public void readStructEnd() throws TException {
    throw up();
  }

  enum ContextType {
    READ,
    WRITE;
  }

  abstract class Context {

    final Context parent;
    final BaseContext base;
    Context(Context parent) {
      this.parent = parent;
      this.base = this.parent.base;
    }

    abstract StructContext newStruct() throws TException;
    
    abstract Context writeStart() throws TException;
    abstract Context readStart() throws TException;
    
    abstract Context writeEnd() throws TException;
    abstract Context readEnd() throws TException;

    protected final BaseContext base() {
      if (this instanceof BaseContext) {
        return (BaseContext) this;
      } else {
        return this.parent.base();
      }
    }
    protected final ContextType type() {
      return base().type;
    }
    Context peek() {
      return base().peek();
    }
    Context pop() {
      return base().pop();
    }
    <T extends Context> T push(T c) {
      return base().push(c);
    }
    <T extends Context> T peek(Class<T> type) {
      return _ensure(type, peek());
    }
    <T extends Context> T pop(Class<T> type) {
      return _ensure(type, pop());
    }
    final void debug(String op) {
      if (LOG.isDebugEnabled()) {
        final StringBuilder sb = new StringBuilder();
        sb.append(type().name().toLowerCase());
        if (op != null) {
          sb.append(' ').append(op).append(": ");
        }
        for (Context top = peek().parent; top != null; top = top.parent) {
          sb.append("  ");
        }
        sb.append(toString());
        LOG.debug(sb.toString());
      }
    }
  }

  abstract class BaseContext extends Context {

    private Context head = this;
    private final ContextType type;

    BaseContext(ContextType type) {
      super(null);
      if (type == null) {
        throw new IllegalArgumentException("type cannot be null.");
      }
      this.type = type;
    }

    @Override final Context writeStart() throws TException { throw up(); }
    @Override final Context readStart() throws TException { throw up(); }
    @Override final Context writeEnd() throws TException { throw up(); }
    @Override final Context readEnd() throws TException { throw up(); }

    @Override Context peek() {
      return head;
    }

    @Override <T extends Context> T push(final T context) {
      if (context.parent != head) {
        throw new IllegalArgumentException(
          "new context's parent must match the current top of stack");
      }
      this.head = context;
      this.head.debug("push");
      return context;
    }

    @Override Context pop() {
      if (this.head == this) {
        throw new IllegalStateException("Cannot pop the base context.");
      }
      final Context oldhead = this.head;
      oldhead.debug("pop");
      this.head = oldhead.parent;
      return oldhead;
    }

    abstract MessageContext newMessage() throws TException;

  }

  abstract class MessageContext extends Context {
    MessageContext(Context parent) {
      super(parent);
    }
    @Override MessageContext writeStart() throws TException { throw up(); }
    @Override MessageContext writeEnd() throws TException { throw up(); }
  }

  abstract class StructContext extends Context {
    String name;
    StructContext(Context parent) {
      super(parent);
    }
    TStruct emit() {
      return new TStruct(name);
    }
    public String toString() {
      return "<TStruct name:'" + name + "'>";
    }
    @Override StructContext writeStart() throws TException {
      writeStartElement(name);
      return this;
    }
    @Override StructContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }
    abstract FieldContext newField() throws TException;
  }

  abstract class FieldContext extends Context {
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
    @Override FieldContext writeStart() throws TException {
      writeStartElement(name);
      writeAttribute("fieldId", Short.toString(id));
      writeAttribute("type", Byte.toString(type));
      return this;
    }
    @Override FieldContext writeEnd() throws TException {
      writeEndElement();
      return this;
    }
    abstract ListContext newList() throws TException;
    abstract SetContext newSet() throws TException;
    abstract MapContext newMap() throws TException;
  }

  abstract class ContainerContext<T> extends Context {
    byte elemType;
    int size;
    final Class<T> emitType;
    final String containerType;
    protected ContainerContext(
        FieldContext field,
        Class<T> emitType, 
        String containerType) {
      super(field);
      this.emitType = emitType;
      this.containerType = containerType;
      if (field == null) {
        throw new IllegalArgumentException("parent field cannot be null.");
      }
    }
    final FieldContext field() {
      return (FieldContext) parent;
    }
    abstract T emit();
    public String toString() {
      return "<"+emitType.getSimpleName()+" type:"+elemType+" size:"+size+">";
    }
    @Override ContainerContext<T> writeStart() throws TException {
      writeStartElement(containerType);
      writeAttribute("size", Integer.toString(size));
      writeAttribute("type", Byte.toString(elemType));
      return this;
    }
    @Override ContainerContext<T> writeEnd() throws TException {
      writeEndElement();
      return this;
    }
  }

  abstract class ListContext extends ContainerContext<TList> {
    ListContext(FieldContext field) {
      super(field, TList.class, "list");
    }
    @Override TList emit() {
      return new TList(elemType, size);
    }
  }

  abstract class SetContext extends ContainerContext<TSet> {
    SetContext(FieldContext field) {
      super(field, TSet.class, "set");
    }
    @Override TSet emit() {
      return new TSet(elemType, size);
    }
  }

  abstract class MapContext extends ContainerContext<TMap> {
    byte keyType;
    MapContext(FieldContext field) {
      super(field, TMap.class, "map");
    }
    @Override TMap emit() {
      return new TMap(keyType, elemType, size);
    }
    @Override public String toString() {
      return "<TMap key:"+keyType+" type:"+elemType+" size:"+size+">";
    }
  }

  private static <T extends Context> T _ensure(Class<T> type, Context ctx) {
    if (type.isAssignableFrom(ctx.getClass())) {
      return type.cast(ctx);
    }
    throw new IllegalArgumentException(
      "Expected " + type.getSimpleName() + 
      " but was actually " + ctx.getClass().getSimpleName()
    );
  }

  protected AbstractXMLProtocol(TTransport trans) {
    super(trans);
    this.writectx = createBaseContext(ContextType.WRITE);
    this.readctx = createBaseContext(ContextType.READ);
  }

  protected abstract BaseContext createBaseContext(ContextType type);

  protected XMLStreamWriter writer() throws XMLStreamException {
    if (__writer == null) {
      __writer = xmlOutputFactory().createXMLStreamWriter(
        new TTransportOutputStream(getTransport())
      );
    }
    return __writer;
  }

  protected final BaseContext readctx;

  protected final BaseContext writectx;

  private XMLStreamWriter __writer;

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

  private static final XMLOutputFactory 
    DEFAULT_XML_OUT = XMLOutputFactory.newFactory();

  protected XMLOutputFactory xmlOutputFactory() {
    return DEFAULT_XML_OUT;
  }

  protected final UnsupportedOperationException up() {
    throw new UnsupportedOperationException();
  }

}
