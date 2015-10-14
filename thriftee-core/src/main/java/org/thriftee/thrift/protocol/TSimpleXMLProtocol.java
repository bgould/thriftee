package org.thriftee.thrift.protocol;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;

public class TSimpleXMLProtocol extends AbstractXMLProtocol {

  public static class Factory implements TProtocolFactory {
    private static final long serialVersionUID = 1017378360734059748L;
    @Override
    public TProtocol getProtocol(TTransport transport) {
      return new TSimpleXMLProtocol(transport);
    }
  }

  class SimpleBaseContext extends BaseContext {
    SimpleBaseContext(ContextType type) {
      super(type);
    }
    @Override MessageContext newMessage() throws TException {
      return new SimpleMessageContext(this);
    }
    @Override StructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }
  }

  class SimpleMessageContext extends MessageContext {
    public SimpleMessageContext(Context parent) {
      super(parent);
    }
    @Override SimpleMessageContext readStart() throws TException { throw up(); }
    @Override SimpleMessageContext readEnd() throws TException { throw up(); }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  class SimpleStructContext extends StructContext {
    SimpleStructContext(Context parent) {
      super(parent);
    }
    @Override SimpleFieldContext readStart() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleFieldContext readEnd() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleFieldContext newField() throws TException {
      return new SimpleFieldContext(this);
    }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  class SimpleFieldContext extends FieldContext {
    String name;
    byte type;
    short id;
    SimpleFieldContext(SimpleStructContext struct) {
      super(struct);
    }
    TField emit() {
      return new TField(name, type, id);
    }
    public String toString() {
      return emit().toString();
    }
    @Override SimpleFieldContext readStart() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleFieldContext readEnd() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleListContext newList() throws TException {
      return null;
    }
    @Override SimpleSetContext newSet() throws TException {
      return null;
    }
    @Override SimpleMapContext newMap() throws TException {
      return null;
    }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  class SimpleListContext extends ListContext {
    SimpleListContext(FieldContext field) {
      super(field);
    }
    @Override SimpleListContext readStart() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleListContext readEnd() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  class SimpleSetContext extends SetContext {
    SimpleSetContext(FieldContext field) {
      super(field);
    }
    @Override SimpleSetContext readStart() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleSetContext readEnd() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  class SimpleMapContext extends MapContext {
    SimpleMapContext(FieldContext field) {
      super(field);
    }
    @Override SimpleMapContext readStart() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleMapContext readEnd() throws TException {
      throw new UnsupportedOperationException();
    }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  public TSimpleXMLProtocol(TTransport trans) {
    super(trans);
  }

  @Override
  protected BaseContext createBaseContext(ContextType type) {
    return new SimpleBaseContext(type);
  }

}
