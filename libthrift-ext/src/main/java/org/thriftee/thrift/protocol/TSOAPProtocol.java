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
package org.thriftee.thrift.protocol;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static org.apache.thrift.TApplicationException.INTERNAL_ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.thriftee.thrift.schema.AbstractFieldSchema;
import org.thriftee.thrift.schema.AbstractStructSchema;
import org.thriftee.thrift.schema.ContainerSchemaType;
import org.thriftee.thrift.schema.ListSchemaType;
import org.thriftee.thrift.schema.MapSchemaType;
import org.thriftee.thrift.schema.SchemaType;
import org.thriftee.thrift.schema.ServiceSchema;
import org.thriftee.thrift.schema.SetSchemaType;
import org.thriftee.thrift.schema.StructSchema;
import org.thriftee.thrift.transport.TTransportInputStream;
import org.thriftee.thrift.transport.TTransportOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class TSOAPProtocol extends AbstractSimpleProtocol {

  public static final MessageFactory soapMessageFactory;

  public static final DocumentBuilderFactory documentBuilderFactory;

  public static final TransformerFactory tf = TransformerFactory.newInstance();

  public static final String TXP_NS = "http://thriftee.org/xml/protocol";

  public static final String TAEX_FAULT_ACTOR = "txp:application-exception";

  static {
    try {
      soapMessageFactory = MessageFactory.newInstance();
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Factory
   */
  public static class Factory extends AbstractFactory<TSOAPProtocol> {

    private static final long serialVersionUID = -2988163176565419085L;

    public Factory() {
      super();
    }

    public Factory(ServiceSchema baseService, StructSchema baseStruct) {
      super(baseService, baseStruct);
    }

    public Factory(ServiceSchema baseService) {
      super(baseService);
    }

    public Factory(StructSchema baseStruct) {
      super(baseStruct);
    }

    @Override
    protected TSOAPProtocol getProtocol(
        final TTransport trans,
        final ServiceSchema baseService,
        final AbstractStructSchema<?,?,?,?> baseStruct) {
      return new TSOAPProtocol(trans, baseService, baseStruct);
    }

  }

  protected class SoapBaseContext extends AbstractSimpleBaseContext {

    private Object root;

    protected SoapBaseContext(ContextType type,
        ServiceSchema baseService,
        AbstractStructSchema<?, ?, ?, ?> baseStruct) {
      super(type, baseService, baseStruct);
    }

    @Override
    protected SoapMessageContext newMessage(ServiceSchema schema)
        throws TException {
      SOAPMessage msg = isReading() ? parseSoapMessage() : newSoapMessage();
      root = msg;
      try {
        final SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
        env.addNamespaceDeclaration("txp", TXP_NS);
      } catch (SOAPException e) {
        throw ex(e);
      }
      return new SoapMessageContext(this, getBaseService(), msg);
    }

    @Override
    protected SoapStructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
      Document doc = isReading() ? parseDocument() : newDocument();
      root = doc;
      final SoapValueHolder holder = new SoapValueHolder(
        isReading() ? it(doc).next() : doc
      );
      holder.setNamespace(schema.getModule().getXmlTargetNamespace());
      holder.setKey(schema.getName());
      return new SoapStructContext(this, getBaseStruct(), holder);
    }

    @Override
    protected ServiceSchema getBaseService() {
      return TSOAPProtocol.this.getBaseService();
    }

    @Override
    protected AbstractStructSchema<?, ?, ?, ?> getBaseStruct() {
      return TSOAPProtocol.this.getBaseStruct();
    }

    @Override
    public void popped() throws TException {
      if (isWriting()) {
        try {
          final Source source;
          if (root instanceof SOAPMessage) {
            source = ((SOAPMessage)root).getSOAPPart().getContent();
//            ((SOAPMessage)root).writeTo(out);
          } else if (root instanceof Document) {
            source = new DOMSource((Document)root);
          } else {
            throw new IllegalStateException();
          }
          final TTransport trans = getTransport();
          final OutputStream out = TTransportOutputStream.outputStreamFor(trans);
          final StreamResult result = new StreamResult(out);
          final Transformer transformer = tf.newTransformer();
//          transformer.setOutputProperty(OutputKeys.INDENT, "true");
          transformer.transform(source, result);
        } catch (Exception e) {
          throw ex(e);
        }
      }
    }

  }

  protected class SoapMessageContext extends AbstractSimpleMessageContext {

    private final SOAPMessage msg;

    public SoapMessageContext(
        final Context parent,
        final ServiceSchema svc,
        final SOAPMessage soapMessage) {
      super(parent, svc);
      this.msg = soapMessage;
    }

    @Override
    public SoapMessageContext writeStart() throws TException {
      final String el = byteToMessageType(messageType());
      try {
        final SOAPElement soapElement = msg.getSOAPHeader().addChildElement(
          msg.getSOAPHeader().createQName(el, "txp")
        );
        soapElement.addAttribute(new QName("method"), name());
        soapElement.addAttribute(new QName("seqid"), seqid()+"");
        if (messageType() == TMessageType.EXCEPTION) {
          final SOAPFault fault = msg.getSOAPBody().addFault(
            new QName("SOAP-ENV:Server"),
            "An application error occurred."
          );
          fault.setFaultActor(TAEX_FAULT_ACTOR);
          fault.addDetail();
        }
        msg.getSOAPHeader().detachNode();
      } catch (SOAPException e) {
        throw ex(e);
      }

      return this;
    }

    @Override
    public SoapMessageContext readStart() throws TException {
      // if there is a txp: element in the soap header, use the info from that
      try {
        final SOAPHeader soapHeader = msg.getSOAPHeader();
        if (soapHeader != null) {
          for (Element el : elements(soapHeader)) {
            if (TXP_NS.equals(el.getNamespaceURI())) {
              final byte typeValue = messageTypeToByte(el.getLocalName());
              final NamedNodeMap attrs = el.getAttributes();
              final Node nameNode = attrs.getNamedItem("method");
              if (nameNode != null) {
                final String method = nameNode.getNodeValue();
                final Node seqidNode = attrs.getNamedItem("seqid");
                final int seqid;
                if (seqidNode == null) {
                  seqid = 1;
                } else {
                  try {
                    seqid = Integer.parseInt(seqidNode.getNodeValue());
                  } catch (NumberFormatException e) {
                    throw ex("Invalid seqid: " + seqidNode.getNodeValue());
                  }
                }
                set(method, typeValue, seqid);
                return this;
              } else {
                throw ex("Method name missing from txp:" + el.getLocalName());
              }
            }
          }
        }
        // if there is no txp: element, inspect the SOAP body for message info
        final SOAPBody soapBody = msg.getSOAPBody();
        if (msg.getSOAPBody().getFault() == null) {
          ElementIterator it = it(soapBody);
          if (!it.hasNext()) {
            throw ex("SOAPBody had no elements");
          }
          final Element firstElement = it.next();
          final String elName = firstElement.getLocalName();
          if (elName == null) {
            throw ex("First element in SOAP body had no local name?");
          }
          byte messageType;
          String methodName;
          if (elName.endsWith("Request")) {
            messageType = TMessageType.CALL;
            methodName = elName.substring(0, elName.length() - 7);
          } else if (elName.endsWith("Response")) {
            messageType = TMessageType.REPLY;
            methodName = elName.substring(0, elName.length() - 8);
          } else {
            throw ex("Unrecognized SOAP body element: " + elName);
          }
          set(methodName, messageType, 1);
          return this;
        } else { // SOAPBody.getFault() != null
          final String actor = soapBody.getFault().getFaultActor();
          final Detail detail = soapBody.getFault().getDetail();
          if (actor == null) {
            throw ex("fault actor was null");
          }
          if (detail == null) {
            throw ex("fault detail was null");
          }
          byte messageType = -1;
          String methodName;
          if (TAEX_FAULT_ACTOR.equals(actor)) {
            messageType = TMessageType.EXCEPTION;
            methodName = ""; // don't think EXCEPTION needs a method name?
          } else {
            throw ex("SOAP faults for user exceptions not implemented yet");
          }
          set(methodName, messageType, 1);
          if (messageType > -1) {
            return this;
          }
        }
        throw ex("Could not read message information from SOAP body");
      } catch (SOAPException e) {
        throw ex(e);
      }
    }

    @Override
    public SoapMessageContext writeEnd() throws TException {
      return this;
    }

    @Override
    public SoapMessageContext readEnd() throws TException {
      return this;
    }

    @Override
    public StructContext newStruct(
            final AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
      try {
        final SoapValueHolder holder = new SoapValueHolder(msg.getSOAPBody());
        switch (messageType()) {
        case TMessageType.CALL:
        case TMessageType.ONEWAY:
          holder.setNamespace(svc.getXmlTargetNamespace());
          holder.setKey(name() + "Request");
          break;
        case TMessageType.REPLY:
          holder.setNamespace(svc.getXmlTargetNamespace());
          holder.setKey(name() + "Response");
          break;
        case TMessageType.EXCEPTION:
          holder.setNamespace(TXP_NS);
          holder.setKey("TApplicationException");
          holder.setParentNode(msg.getSOAPBody().getFault().getDetail());
          break;
        }
        if (isReading()) {
          holder.setParentNode(it(holder.getParentNode()).next());
        }
        return new SoapStructContext(this, schema, holder);
      } catch (SOAPException e) {
        throw ex(e);
      }
    }

    @Override
    public String toString() {
      return "<TMessage name:'"+name()+"' "
            + "type: "+messageType()+" seqid:"+seqid()+">";
    }

  }

  protected class SoapStructContext
        extends AbstractSimpleStructContext<SoapFieldContext> {

    private final SoapValueHolder val;

    private ElementIterator fieldIterator;

    public SoapStructContext(
        final Context parent,
        final AbstractStructSchema<?, ?, ?, ?> structSchema,
        final SoapValueHolder holder) {
      super(parent, structSchema);
      this.val = requireNonNull(holder, "struct valueHolder was null");
//      if (isReading()) {
//        requireNonNull((JsonObject) this.valueHolder.getValue());
//      }
    }

    @Override
    protected AbstractFieldSchema<?, ?> nextFieldSchema() throws TException {
      while (fieldIterator.hasNext()) {
        final Element next = this.fieldIterator.next();
        final String key = next.getNodeName();
        final AbstractFieldSchema<?, ?> field = schema().getFields().get(key);
        if (field != null) {
          return field;
        }
      }
      return null;
    }

    @Override
    public StructContext writeStart() throws TException {
      val.createElement();
      return this;
    }

    @Override
    public StructContext writeEnd() throws TException {
      return this;
    }

    @Override
    public StructContext writeFieldStop() throws TException {
      return this;
    }

    @Override
    public StructContext readStart() throws TException {
//      final ElementIterator it = valueHolder.iterator();
//      if (it.hasNext()) {
//        structElement = it.next();
//        this.fieldIterator = it(structElement);
      this.fieldIterator = val.iterator();
      return this;
//      } else {
//        throw ex("Expected at least one element for struct.");
//      }
    }

    @Override
    public StructContext readEnd() throws TException {
      this.fieldIterator = null;
      return this;
    }

    @Override
    protected SoapFieldContext newField(AbstractFieldSchema<?, ?> schema)
        throws TException {
      if (isReading()) {
        final SoapValueHolder holder = new SoapValueHolder(val.getParentNode());
        holder.setKey(schema.getName());
        return new SoapFieldContext(this, schema, holder);
      } else {
        return new SoapFieldContext(this, null, val.fromLastElement());
      }
    }

  }

  protected class SoapFieldContext
      extends AbstractSimpleFieldContext
      implements FieldContext {

    private final SoapValueHolder val;

    public SoapFieldContext(
        final SoapStructContext parent,
        final AbstractFieldSchema<?, ?> fieldSchema,
        final SoapValueHolder valueHolder) {
      super(parent, fieldSchema);
      this.val = requireNonNull(valueHolder);
    }

    @Override
    public SoapFieldContext writeStart() throws TException {
      this.val.setKey(schema().getName());
      return this;
    }

    @Override
    public SoapFieldContext writeEnd() throws TException {
      return this;
    }

    @Override
    public SoapFieldContext readStart() throws TException {
      return this;
    }

    @Override
    public SoapFieldContext readEnd() throws TException {
      return this;
    }

    @Override
    protected SoapValueHolder getValueHolder() {
      return val;
    }

    @Override
    protected StructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> structSchema) throws TException {
      if (isReading()) {
        SoapValueHolder holder = new SoapValueHolder(val.readElement());
        holder.setKey(schema().getName());
        return new SoapStructContext(this, structSchema, holder);
      } else {
        SoapValueHolder holder = new SoapValueHolder(val.getParentNode());
        holder.setKey(schema().getName());
        return new SoapStructContext(this, structSchema, holder);
      }
    }

    @Override
    protected ListContext newList(ListSchemaType listSchema) throws TException {
      final Element el = isReading() ? val.readElement() : val.createElement();
      return new SoapListContext(this, listSchema, new SoapArrayValueHolder(el));
    }

    @Override
    protected SetContext newSet(SetSchemaType schema) throws TException {
      final Element el = isReading() ? val.readElement() : val.createElement();
      return new SoapSetContext(this, schema, new SoapArrayValueHolder(el));
    }

    @Override
    protected MapContext newMap(MapSchemaType mapSchema) throws TException {
      final Element el = isReading() ? val.readElement() : val.createElement();
      return new SoapMapContext(this, mapSchema, new SoapMapValueHolder(el));
    }

    @Override
    protected SchemaType getSchemaType() throws TException {
      return schema().getType();
    }

    @Override
    protected void beforeRead() throws TException {}

    @Override
    protected void beforeWrite() throws TException {}

  }

  protected abstract class SoapContainerContext<T>
      extends AbstractSimpleContainerContext<T> {

    protected SoapContainerValueHolder container;

    protected SoapContainerContext(
        final Context parent,
        final ContainerSchemaType schemaType,
        final Class<T> emitType,
        final ContainerType containerType,
        final SoapContainerValueHolder container) {
      super(parent, schemaType, emitType, containerType);
      this.container = requireNonNull(container, "valueHolder was null");
    }

    @Override
    public SoapContainerContext<T> writeStart() throws TException {
      container.writeStart();
      return this;
    }

    @Override
    public SoapContainerContext<T> writeEnd() throws TException {
      container.writeEnd();
      return this;
    }

    @Override
    public SoapContainerContext<T> readStart() throws TException {
      //container.readStart();  // ??
      return this;
    }

    @Override
    public SoapContainerContext<T> readEnd() throws TException {
      //container.readEnd();  // ??
      return this;
    }

    @Override
    public SoapValueHolder getValueHolder() {
      return container;
    }

    @Override
    public void beforeRead() throws TException {
      container.next();
    }

    @Override
    public void beforeWrite() throws TException {

    }

    @Override
    protected int size() throws TException {
      return container.size();
    }

    @Override
    protected StructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> structSchema) throws TException {
      if (isReading()) {
        final Element element = getValueHolder().readElement();
        final SoapValueHolder holder = new SoapValueHolder(element);
        return new SoapStructContext(this, structSchema, holder);
      } else {
//      if (isReading()) {
//        holder.setParentNode(getValueHolder().readElement());
//      }
        return new SoapStructContext(this, structSchema, getValueHolder());
      }
    }

    @Override
    protected ListContext newList(ListSchemaType listSchema) throws TException {
      final SoapContainerValueHolder val = container;
      final Element el = isReading() ? val.readElement() : val.createElement();
      return new SoapListContext(this, listSchema, new SoapArrayValueHolder(el));
    }

    @Override
    protected SetContext newSet(SetSchemaType setSchema) throws TException {
      final SoapContainerValueHolder val = container;
      final Element el = isReading() ? val.readElement() : val.createElement();
      return new SoapSetContext(this, setSchema, new SoapArrayValueHolder(el));
    }

    @Override
    protected MapContext newMap(MapSchemaType mapSchema) throws TException {
      final SoapContainerValueHolder val = container;
      final Element el = isReading() ? val.readElement() : val.createElement();
      return new SoapMapContext(this, mapSchema, new SoapMapValueHolder(el));
    }
  }

  protected class SoapListContext
      extends SoapContainerContext<TList>
      implements ListContext {

    public SoapListContext(
        final Context parent,
        final ListSchemaType schema,
        final SoapArrayValueHolder valueHolder) throws TException {
      super(parent, schema, TList.class, ContainerType.LIST, valueHolder);
    }

    @Override
    public TList emit() throws TException {
      return emitList();
    }

    @Override
    public void set(TList obj) throws TException {
      setList(obj);
    }

    @Override
    protected SchemaType getSchemaType() throws TException {
      return getContainerSchemaType().getValueType();
    }

  }

  protected class SoapSetContext
      extends SoapContainerContext<TSet>
      implements SetContext {

    public SoapSetContext(
        final Context parent,
        final SetSchemaType schema,
        final SoapArrayValueHolder valueHolder) throws TException {
      super(parent, schema, TSet.class, ContainerType.SET, valueHolder);
    }

    @Override
    public TSet emit() throws TException {
      return emitSet();
    }

    @Override
    public void set(TSet obj) throws TException {
      setSet(obj);
    }

    @Override
    protected SchemaType getSchemaType() throws TException {
      return getContainerSchemaType().getValueType();
    }

  }

  protected class SoapMapContext
      extends SoapContainerContext<TMap>
      implements MapContext {

    protected boolean isKey = false;

    private SoapValueHolder _valueHolder;

    public SoapMapContext(
        final Context parent,
        final MapSchemaType schema,
        final SoapMapValueHolder valueHolder) throws TException {
      super(parent, schema, TMap.class, ContainerType.MAP, valueHolder);
      _valueHolder = new SoapValueHolder(valueHolder.getParentNode());
      _valueHolder.setKey("entry");
    }

    @Override
    protected SchemaType getSchemaType() throws TException {
      return isKey
        ? ((MapSchemaType) getContainerSchemaType()).getKeyType()
        : getContainerSchemaType().getValueType();
    }

    @Override
    public TMap emit() throws TException {
      return emitMap();
    }

    @Override
    public void set(TMap obj) throws TException {
      setMap(obj);
    }

    @Override
    public void beforeWrite() throws TException {
      isKey=!isKey;
      if (isKey) {
        final Element element = _valueHolder.createElement();
        getValueHolder().setParentNode(element);
        getValueHolder().setKey("key");
      } else {
        getValueHolder().setKey("value");
      }
      super.beforeWrite();
    }
  }

  protected class SoapMapValueHolder extends SoapArrayValueHolder {

    private boolean isKey = false;

    public SoapMapValueHolder(Node parentNode) {
      super(parentNode);
      setKey("entry");
    }

    @Override
    protected boolean advance() throws TException {
      if (isKey) {
        isKey = false;
        return true;
      } else {
        isKey = true;
        return super.advance();
      }
    }

    @Override
    public Element readElement() throws TException {
      currentValue().setKey(isKey ? "key" : "value");
      return currentValue.readElement();
    }

  }

  protected class SoapValueHolder implements ValueHolder, Iterable<Element> {

    private Node _parentNode;

    private String _ns;

    private String _key;

    private Element _element;

    public SoapValueHolder(Node value) {
      this._parentNode = value;
    }

    public void setParentNode(Node value) {
      this._parentNode = value;
    }

    public Node getParentNode() {
      return this._parentNode;
    }

    public String getKey() {
      return _key;
    }

    public void setKey(String key) {
      this._element = null;
      this._key = key;
    }

    public String getNamespace() {
      return _ns;
    }

    public void setNamespace(String namespace) {
      this._ns = namespace;
    }

    protected Node value() {
      return requireNonNull(getParentNode(), "Node should not be null");
    }

    @Override
    public void writeBinary(ByteBuffer buffer) throws TException {
      final byte[] arr = buffer.array();
      write(DatatypeConverter.printBase64Binary(arr));
    }

    @Override
    public void writeBool(boolean bool) throws TException {
      write(DatatypeConverter.printBoolean(bool));
    }

    @Override
    public void writeByte(byte bite) throws TException {
      write(DatatypeConverter.printByte(bite));
    }

    @Override
    public void writeDouble(double dbl) throws TException {
      write(DatatypeConverter.printDouble(dbl));
    }

    @Override
    public void writeI16(short i16) throws TException {
      write(DatatypeConverter.printShort(i16));
    }

    @Override
    public void writeI32(int i32) throws TException {
      write(DatatypeConverter.printInt(i32));
    }

    @Override
    public void writeI64(long i64) throws TException {
      write(DatatypeConverter.printLong(i64));
    }

    @Override
    public void writeString(String str) throws TException {
      write(str);
    }

    @Override
    public ByteBuffer readBinary() throws TException {
      return ByteBuffer.wrap(parseBase64Binary(readString()));
    }

    @Override
    public boolean readBool() throws TException {
      return DatatypeConverter.parseBoolean(valueToRead());
    }

    @Override
    public byte readByte() throws TException {
      return DatatypeConverter.parseByte(valueToRead());
    }

    @Override
    public double readDouble() throws TException {
      return DatatypeConverter.parseDouble(valueToRead());
    }

    @Override
    public short readI16() throws TException {
      return DatatypeConverter.parseShort(valueToRead());
    }

    @Override
    public int readI32() throws TException {
      return DatatypeConverter.parseInt(valueToRead());
    }

    @Override
    public long readI64() throws TException {
      return DatatypeConverter.parseLong(valueToRead());
    }

    @Override
    public String readString() throws TException {
      return valueToRead();
    }

    public Element createElement() {
      final Node parentNode = getParentNode();
      final String key = getKey();
      if (key == null) {
        throw new IllegalStateException("key should not be null");
      }
      if (parentNode == null) {
        throw new IllegalStateException("value should not be null");
      }
      final Document doc = (parentNode instanceof Document)
          ? (Document) parentNode : parentNode.getOwnerDocument();
      final Element newElement;
      if (getNamespace() != null) {
        newElement = doc.createElementNS(getNamespace(), "svc:"+key);
      } else {
        newElement = doc.createElementNS(null, key);
      }
      parentNode.appendChild(newElement);
      this._element = newElement;
      return this._element;
    }

    protected void write(String str) throws TException {
      createElement().setTextContent(str);
    }

    protected String valueToRead() throws TException {
      return readElement().getTextContent();
    }

    public Element readElement() throws TException {
      final Node parentNode = getParentNode();
      final String key = getKey();
      if (key == null) {
        throw new IllegalStateException("key should not be null");
      }
      if (parentNode == null) {
        throw new IllegalStateException("value should not be null");
      }
      if (lastElement() != null) {
        throw new UnsupportedOperationException();
//        return lastElement();
      } else {
        for (Element el : this) {
          if (key.equals(el.getNodeName())) {
            this._element = el;
            return el;
          }
        }
      }
      throw ex("No mapping for key: " + key);
    }

    protected Element lastElement() {
      return this._element;
    }

    public SoapValueHolder fromLastElement() {
      final SoapValueHolder holder = new SoapValueHolder(lastElement());
      return holder;
    }

    @Override
    public ElementIterator iterator() {
      return it(getParentNode());
    }
  }

  protected abstract class SoapContainerValueHolder extends SoapValueHolder {
    public SoapContainerValueHolder(Node value) {
      super(value);
    }
    final boolean next() throws TException {
      if (advance()) {
        return true;
      } else {
        return false;
      }
    }
    protected abstract int size() throws TException;
    protected abstract boolean advance() throws TException;
    protected abstract void readStart() throws TException;
    protected abstract void readEnd() throws TException;
    protected abstract void writeStart() throws TException;
    protected abstract void writeEnd() throws TException;
    protected abstract SoapValueHolder currentValue() throws TException;
  }

  protected class SoapArrayValueHolder extends SoapContainerValueHolder {

    private Integer _size = null;
    private ElementIterator iterator;
    protected final SoapValueHolder currentValue = new SoapValueHolder(null);

    public SoapArrayValueHolder(Node parentNode) {
      super(parentNode);
      setKey("item");
    }

    @Override
    protected int size() throws TException {
      if (this._size == null) {
        int size = 0;
        for (ElementIterator it = it(getParentNode()); it.hasNext(); ) {
          it.next();
          size++;
        }
        this._size = size;
      }
      return this._size;
    }

    @Override
    protected boolean advance() throws TException {
      if (this.iterator == null) {
        this.iterator = it(getParentNode());
      }
      currentValue.setKey(null);
      if (this.iterator.hasNext()) {
        currentValue.setParentNode(this.iterator.next());
        return true;
      } else {
        currentValue.setParentNode(null);
        return false;
      }
    }

    @Override
    protected SoapValueHolder currentValue() throws TException {
      return this.currentValue;
    }

    @Override
    public Element readElement() throws TException {
      return (Element) currentValue.getParentNode();
    }

    @Override
    protected String valueToRead() throws TException {
      if (this.iterator == null) {
        throw ex("iterator not initialized");
      }
      if (this.currentValue.getParentNode() == null) {
        throw ex("no lastElement");
      }
      return readElement().getTextContent();
    }

    @Override
    protected void writeStart() throws TException {
//      this.createElement();
//      if (getKey() != null) {
//        writeStartArray(getKey());
//        setKey(null);
//      } else {
//        writeStartArray();
//      }
    }

    @Override
    protected void writeEnd() throws TException {
    }

    @Override
    protected void readStart() throws TException {
    }

    @Override
    protected void readEnd() throws TException {
    }

  }

  static final byte messageTypeToByte(String element) throws TException {
    for (int i = 1; i < 5; i++) {
      if (MESSAGE_TYPES[i].equals(element)) {
        return (byte) i;
      }
    }
    throw ex("Invalid message type name: " + element);
  }

  static final String byteToMessageType(byte type) throws TException {
    try {
      return MESSAGE_TYPES[type];
    } catch (ArrayIndexOutOfBoundsException e) {
      throw ex("Invalid message type: " + Integer.toString(type, 16));
    }
  }

  @Override
  protected BaseContext createBaseContext(ContextType type) {
    return new SoapBaseContext(type, getBaseService(), getBaseStruct());
  }

  protected TSOAPProtocol(
      final TTransport trans,
      final ServiceSchema baseService,
      final AbstractStructSchema<?,?,?,?> baseStruct) {
    super(trans, baseService, baseStruct);
  }

  protected SOAPMessage parseSoapMessage() throws TException {
    final InputStream in = TTransportInputStream.inputStreamFor(getTransport());
    try {
      final SOAPMessage msg = soapMessageFactory.createMessage(null, in);
      return msg;
    } catch (final SOAPException e) {
      throw new TProtocolException(e);
    } catch (final IOException e) {
      throw new TTransportException(e);
    }
  }

  protected SOAPMessage newSoapMessage() throws TException {
    try {
      final SOAPMessage msg = soapMessageFactory.createMessage();
      return msg;
    } catch (SOAPException e) {
      throw new TProtocolException(e);
    }
  }

  protected Document parseDocument() throws TException {
    final InputStream in = TTransportInputStream.inputStreamFor(getTransport());
    try {
      final DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
      final Document doc = b.parse(in);
      return doc;
    } catch (final ParserConfigurationException e) {
      throw new TApplicationException(INTERNAL_ERROR, e.getMessage());
    } catch (final SAXException e) {
      throw new TProtocolException(e);
    } catch (final IOException e) {
      throw new TTransportException(e);
    }
  }

  protected Document newDocument() throws TException {
    try {
      final DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
      final Document doc = b.newDocument();
      return doc;
    } catch (final ParserConfigurationException e) {
      throw new TApplicationException(INTERNAL_ERROR, e.getMessage());
    }
  }

  private static final String[] MESSAGE_TYPES = new String[5]; static {
    try {
      for (final Field field : TMessageType.class.getDeclaredFields()) {
        if (isStatic(field.getModifiers()) && field.getType() == Byte.TYPE) {
          MESSAGE_TYPES[field.getByte(null)] = field.getName().toLowerCase();
        }
      }
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static Iterable<Element> elements(final Node node) {
    return new Iterable<Element>() {
      @Override
      public Iterator<Element> iterator() {
        return it(node);
      }
    };
  }

  public static ElementIterator it(Node node) {
    return new ElementIterator(node);
  }

  public static class ElementIterator implements Iterator<Element>{

    private Element __next;

    public ElementIterator(Node node) {
      for (Node child = node.getFirstChild();
                child != null;
                child = child.getNextSibling()) {
        if (child.getNodeType() == Node.ELEMENT_NODE) {
          __next = (Element) child;
          return;
        }
      }
    }

    @Override
    public boolean hasNext() {
      return __next != null;
    }

    @Override
    public Element next() {
      if (__next == null) {
        throw new NoSuchElementException();
      }
      final Element result = __next;
      __next = null;
      for (Node sib = result.getNextSibling();
                sib != null; sib = sib.getNextSibling()) {
        if (sib.getNodeType() == Node.ELEMENT_NODE) {
          __next = (Element) sib;
          break;
        }
      }
      return result;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

}
