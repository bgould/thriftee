package org.thriftee.thrift.protocol;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.nio.ByteBuffer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TException;
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

  protected static final DocumentBuilderFactory documentBuilderFactory =
    DocumentBuilderFactory.newInstance();

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
    @Override SimpleMessageContext readStart() throws TException {throw up();}
    @Override SimpleMessageContext readEnd()   throws TException {throw up();}
    @Override SimpleStructContext  newStruct() throws TException {throw up();}
  }

  class SimpleStructContext extends StructContext {
    SimpleStructContext(Context parent) {
      super(parent);
    }
    @Override SimpleStructContext readStart() throws TException {
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
    @Override SimpleStructContext readEnd() throws TException {
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
    @Override SimpleFieldContext newField() throws TException {
      return new SimpleFieldContext(this);
    }
    @Override SimpleStructContext newStruct() throws TException {
      throw up();
    }
  }

  class SimpleFieldContext extends FieldContext {
    SimpleFieldContext(SimpleStructContext struct) {
      super(struct);
    }
    @Override SimpleFieldContext readStart() throws TException {
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
    @Override SimpleFieldContext readEnd() throws TException {
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
    @Override SimpleListContext newList() throws TException {
      return new SimpleListContext(this);
    }
    @Override SimpleSetContext newSet() throws TException {
      return new SimpleSetContext(this);
    }
    @Override SimpleMapContext newMap() throws TException {
      return new SimpleMapContext(this);
    }
    @Override SimpleStructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }
  }

  class SimpleListContext extends ListContext {
    SimpleListContext(FieldContext field) {
      super(field);
    }
    @Override SimpleListContext readStart() throws TException {
      readContainerStart(this);
      return this;
    }
    @Override SimpleListContext readEnd() throws TException {
      readContainerEnd(this);
      return this;
    }
    @Override SimpleStructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }
  }

  class SimpleSetContext extends SetContext {
    SimpleSetContext(FieldContext field) {
      super(field);
    }
    @Override SimpleSetContext readStart() throws TException {
      readContainerStart(this);
      return this;
    }
    @Override SimpleSetContext readEnd() throws TException {
      readContainerEnd(this);
      return this;
    }
    @Override SimpleStructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }
  }

  class SimpleMapContext extends MapContext {
    SimpleMapContext(FieldContext field) {
      super(field);
    }
    @Override SimpleMapContext readStart() throws TException {
      readContainerStart(this);
      return this;
    }
    @Override SimpleMapContext readEnd() throws TException {
      readContainerEnd(this);
      return this;
    }
    @Override SimpleStructContext newStruct() throws TException {
      return new SimpleStructContext(this);
    }
  }

  private void readContainerStart(ContainerContext<?> ctx) throws TException {
    final int etype = reader().getEventType();
    if (etype == START_ELEMENT) {
      final String ctype = readAttribute("ctype");
      if (!(ctx.containerType.equals(ctype))) {
        throw new IllegalStateException(
          "Expected '" + ctx.containerType + "' but was '" + ctype + "'");
      }
      ctx.size = readIntAttribute("csize");
      ctx.elemType = readByteAttribute("vtype");
      if ("map".equals(ctype)) {
        ((MapContext)ctx).keyType = readByteAttribute("ktype");
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
