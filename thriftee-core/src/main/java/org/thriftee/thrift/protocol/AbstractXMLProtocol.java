package org.thriftee.thrift.protocol;

import java.nio.ByteBuffer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.thrift.transport.TTransportInputStream;
import org.thriftee.thrift.transport.TTransportOutputStream;

public abstract class AbstractXMLProtocol extends TProtocol {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  /*--------------------------- Write Methods ------------------------------*/
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
  public void writeStructBegin(TStruct struct) throws TException {
    final StructContext ctx = writectx.peek().newStruct();
    ctx.name = struct.name;
    ctx.push();
    ctx.writeStart();
  }

  @Override
  public void writeStructEnd() throws TException {
    writectx.peek().writeEnd().pop();
  }

  @Override
  public void writeFieldBegin(TField field) throws TException {
    final FieldContext ctx = writectx.peek(StructContext.class).newField();
    ctx.id = field.id;
    ctx.name = field.name;
    ctx.type = field.type;
    ctx.push();
    ctx.writeStart();
  }

  @Override
  public void writeFieldEnd() throws TException {
    writectx.peek(FieldContext.class).writeEnd().pop();
  }

  @Override
  public void writeFieldStop() throws TException {
    writectx.peek(StructContext.class).writeFieldStop();
  }

  @Override
  public void writeListBegin(TList list) throws TException {
    final ListContext ctx = writectx.peek(FieldContext.class).newList();
    ctx.elemType = list.elemType;
    ctx.size = list.size;
    ctx.push();
    ctx.writeStart();
  }

  @Override
  public void writeListEnd() throws TException {
    writectx.peek(ListContext.class).writeEnd().pop();
  }

  @Override
  public void writeMapBegin(TMap map) throws TException {
    final MapContext ctx = writectx.peek(FieldContext.class).newMap();
    ctx.keyType = map.keyType;
    ctx.elemType = map.valueType;
    ctx.size = map.size;
    ctx.push();
    ctx.writeStart();
  }

  @Override
  public void writeMapEnd() throws TException {
    writectx.peek(MapContext.class).writeEnd().pop();
  }

  @Override
  public void writeSetBegin(TSet set) throws TException {
    final SetContext ctx = writectx.peek(FieldContext.class).newSet();
    ctx.elemType = set.elemType;
    ctx.size = set.size;
    ctx.push();
    ctx.writeStart();
  }

  @Override
  public void writeSetEnd() throws TException {
    writectx.peek(SetContext.class).writeEnd().pop();
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

  /*--------------------------- Read Methods -------------------------------*/
  @Override
  public TMessage readMessageBegin() throws TException {
    throw up();
  }

  @Override
  public void readMessageEnd() throws TException {
    throw up();
  }

  @Override
  public TStruct readStructBegin() throws TException {
    final StructContext struct = readctx.peek().newStruct();
    struct.readStart().push();
    return struct.emit();
  }

  @Override
  public void readStructEnd() throws TException {
    readctx.peek(StructContext.class).readEnd().pop();
  }

  @Override
  public TField readFieldBegin() throws TException {
    final FieldContext ctx = readctx.peek(StructContext.class).newField();
    ctx.readStart().push();
    if (ctx.type == TType.STOP) {
      ctx.readEnd().pop();
      return new TField();
    } else {
      return ctx.emit();
    }
  }

  @Override
  public void readFieldEnd() throws TException {
    readctx.peek(FieldContext.class).readEnd().pop();
  }

  @Override
  public TList readListBegin() throws TException {
    final ListContext ctx = readctx.peek(FieldContext.class).newList();
    ctx.readStart().push();
    return ctx.emit();
  }

  @Override
  public void readListEnd() throws TException {
    readctx.peek(ListContext.class).readEnd().pop();
  }

  @Override
  public TMap readMapBegin() throws TException {
    final MapContext ctx = readctx.peek(FieldContext.class).newMap();
    ctx.readStart().push();
    return ctx.emit();
  }

  @Override
  public void readMapEnd() throws TException {
    readctx.peek(MapContext.class).readEnd().pop();
  }

  @Override
  public TSet readSetBegin() throws TException {
    final SetContext ctx = readctx.peek(FieldContext.class).newSet();
    ctx.readStart().push();
    return ctx.emit();
  }

  @Override
  public void readSetEnd() throws TException {
    readctx.peek(SetContext.class).readEnd().pop();
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
  public String readString() throws TException {
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

  /*--------------------------- Context API -------------------------------*/
  enum ContextType {
    READ,
    WRITE;
  }

  abstract class Context {

    final Context parent;
    final BaseContext base;
    Context(Context parent) {
      this.parent = parent;
      if (this instanceof BaseContext) {
        this.base = (BaseContext) this;
      } else {
        this.base = this.parent.base;
      }
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
    Context push() {
      return base().push(this);
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

    <T extends Context> T push(final T context) {
      if (context.parent != head) {
        throw new IllegalArgumentException(
          "new context's parent must match the current top of stack");
      }
      this.head = context;
      //this.head.debug("push");
      return context;
    }

    @Override Context pop() {
      if (this.head == this) {
        throw new IllegalStateException("Cannot pop the base context.");
      }
      final Context oldhead = this.head;
      //oldhead.debug(" pop");
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
    StructContext writeFieldStop() throws TException {
      writeStartElement("_.");
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
      writeAttribute("i", Short.toString(id));
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
      writeAttribute("ctype", containerType);
      writeAttribute("csize", Integer.toString(size));
      writeAttribute("vtype", Byte.toString(elemType));
      return this;
    }
    @Override ContainerContext<T> writeEnd() throws TException {
      //writeEndElement();
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
    @Override MapContext writeStart() throws TException {
      super.writeStart();
      writeAttribute("ktype", Byte.toString(keyType));
      return this;
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

  protected final BaseContext readctx;

  protected final BaseContext writectx;

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

  protected void writeCharacters(String s) throws TException {
    try {
      writer().writeCharacters(s);
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

  protected final UnsupportedOperationException up() {
    throw new UnsupportedOperationException();
  }

}
