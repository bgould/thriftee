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

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class TSOAPProtocol extends AbstractSimpleProtocol {

  public static final MessageFactory soapMessageFactory;

  public static final DocumentBuilderFactory documentBuilderFactory;

  public static final TransformerFactory tf = TransformerFactory.newInstance();

  public static final String TXP_NS = "http://thriftee.org/xml/protocol";

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
      final SoapValueHolder holder = new SoapValueHolder(doc);
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
          final OutputStream out = new TTransportOutputStream(getTransport());
          final StreamResult result = new StreamResult(out);
          final Source source;
          if (root instanceof SOAPMessage) {
            source = ((SOAPMessage)root).getSOAPPart().getContent();
          } else if (root instanceof Document) {
            source = new DOMSource((Document)root);
          } else {
            throw new IllegalStateException();
          }
          final Transformer transformer = tf.newTransformer();
          transformer.setOutputProperty(OutputKeys.INDENT, "true");
          transformer.transform(source, result);
        } catch (TransformerException|SOAPException e) {
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
      } catch (SOAPException e) {
        throw ex(e);
      }
      return this;
    }

    @Override
    public SoapMessageContext readStart() throws TException {
//      msg.getSOAPHeader().
//      if (json == null) throw ex("no json in " + type() + " context");
//      set(
//        expectString(json, ATTRIBUTE_NAME),
//        messageTypeToByte(expectString(json, ATTRIBUTE_TYPE)),
//        json.containsKey(ATTRIBUTE_SEQID) ? expectInt(json, ATTRIBUTE_SEQID) : 1
//      );
      return this;
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
          final SOAPFault fault = msg.getSOAPBody().addFault(
            new QName("SOAP-ENV:Server"),
            "An application error occurred."
          );
          fault.setFaultActor("txp:application-exception");
          holder.setKey("txp:TApplicationException");
          holder.setParentNode(fault.addDetail());
          break;
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

    private final SoapValueHolder valueHolder;

    private Iterator<String> fieldIterator;

    public SoapStructContext(
        final Context parent,
        final AbstractStructSchema<?, ?, ?, ?> structSchema,
        final SoapValueHolder holder) {
      super(parent, structSchema);
      this.valueHolder = requireNonNull(holder, "struct valueHolder was null");
//      if (isReading()) {
//        requireNonNull((JsonObject) this.valueHolder.getValue());
//      }
    }

    @Override
    protected AbstractFieldSchema<?, ?> nextFieldSchema() throws TException {
      while (fieldIterator.hasNext()) {
        final String key = this.fieldIterator.next();
        final AbstractFieldSchema<?, ?> field = schema().getFields().get(key);
        if (field != null) {
          return field;
        }
      }
      return null;
    }

    @Override
    public StructContext writeStart() throws TException {
      valueHolder.createElement();
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
//      this.fieldIterator =
//        ((JsonObject) this.valueHolder.getValue()).keySet().iterator();
      return this;
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
//        final JsonObject json = (JsonObject) valueHolder.getValue();
//        if (json == null) {
//          throw ex("JsonValue should not be null for " + schema);
//        }
//        final JsonValue val = json.get(schema.getName());
//        if (val == null) {
//          throw ex("JsonValue should not be null for " + schema);
//        }
        final SoapValueHolder holder = new SoapValueHolder(null);
        holder.setKey(schema.getName());
//        holder.setValue(json);
//        holder.setValue(val);
        return new SoapFieldContext(this, schema, holder);
      } else {
        return new SoapFieldContext(this, null, valueHolder.fromLastElement());
      }
    }

  }

  protected class SoapFieldContext
      extends AbstractSimpleFieldContext
      implements FieldContext {

    private final SoapValueHolder valueHolder;

    public SoapFieldContext(
        final SoapStructContext parent,
        final AbstractFieldSchema<?, ?> fieldSchema,
        final SoapValueHolder valueHolder) {
      super(parent, fieldSchema);
      this.valueHolder = requireNonNull(valueHolder);
    }

    @Override
    public SoapFieldContext writeStart() throws TException {
      this.valueHolder.setKey(schema().getName());
      return this;
    }

    @Override
    public SoapFieldContext writeEnd() throws TException {
      return this;
    }

    @Override
    public SoapFieldContext readStart() throws TException {
      /*
      final Element element = (Element) valueHolder.getValue();
      final NodeList children = element.getChildNodes();
      for (Node child = element.getFirstChild(); child != null;
            child = element.getNextSibling()) {
        if (child instanceof Element && )
      }
       */
      return this;
    }

    @Override
    public SoapFieldContext readEnd() throws TException {
      return this;
    }

    @Override
    protected SoapValueHolder getValueHolder() {
//      if (isWriting()) {
//        valueHolder.setKey(schema().getName());
//      }
      return valueHolder;
    }

    @Override
    protected StructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> structSchema) throws TException {
      SoapValueHolder holder = new SoapValueHolder(valueHolder.getParentNode());
      holder.setKey(schema().getName());
      return new SoapStructContext(this, structSchema, holder);
//      throw new UnsupportedOperationException();
//      if (isReading()) {
//        final JsonValue value = valueHolder.valueToRead();
//        switch (value.getValueType()) {
//        case OBJECT:
//          final JsonValueHolder holder = new JsonValueHolder();
//          holder.setValue(value);
//          return new SoapStructContext(this, structSchema, holder);
//        default:
//          throw ex("Expected JsonObject but was: " + value.getValueType());
//        }
//      } else {
//        return new SoapStructContext(this, structSchema, getValueHolder());
//      }
    }

    @Override
    protected ListContext newList(ListSchemaType listSchema) throws TException {
      final Element element = valueHolder.createElement();
      return new SoapListContext(
        this,
        listSchema,
        new SoapArrayValueHolder(element, "item")
      );
//      if (isReading()) {
//        final JsonValue value = valueHolder.valueToRead();
//        switch (value.getValueType()) {
//        case ARRAY:
//          return new JsonListContext(
//            this, listSchema, new JsonArrayValueHolder((JsonArray) value));
//        default:
//          throw ex("Expected JsonArray but was: " + value.getValueType());
//        }
//      } else {
//        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
//        holder.setKey(schema().getName());
//        return new JsonListContext(this, listSchema, holder);
//      }
    }

    @Override
    protected SetContext newSet(SetSchemaType setSchema) throws TException {
      final Element element = valueHolder.createElement();
      return new SoapSetContext(
        this,
        setSchema,
        new SoapArrayValueHolder(element, "item")
      );
//      if (isReading()) {
//        final JsonValue value = valueHolder.valueToRead();
//        switch (value.getValueType()) {
//        case ARRAY:
//          return new JsonSetContext(
//            this, setSchema, new JsonArrayValueHolder((JsonArray) value));
//        default:
//          throw ex("Expected JsonArray but was: " + value.getValueType());
//        }
//      } else {
//        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
//        holder.setKey(schema().getName());
//        return new JsonSetContext(this, setSchema, holder);
//      }
    }

    @Override
    protected MapContext newMap(MapSchemaType mapSchema) throws TException {
      final Element element = valueHolder.createElement();
      return new SoapMapContext(
        this,
        mapSchema,
        new SoapArrayValueHolder(element, schema().getName())
      );
//      if (isReading()) {
//        final JsonValue value = valueHolder.valueToRead();
//        switch (value.getValueType()) {
//        case ARRAY:
//          return new JsonMapContext(
//            this, mapSchema, new JsonArrayValueHolder((JsonArray) value));
//        default:
//          throw ex("Expected JsonArray but was: " + value.getValueType());
//        }
//      } else {
//        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
//        holder.setKey(schema().getName());
//        return new JsonMapContext(this, mapSchema, holder);
//      }
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

    private SoapContainerValueHolder valueHolder;

    protected SoapContainerContext(
        final Context parent,
        final ContainerSchemaType schemaType,
        final Class<T> emitType,
        final ContainerType containerType,
        final SoapContainerValueHolder valueHolder) {
      super(parent, schemaType, emitType, containerType);
      this.valueHolder = requireNonNull(valueHolder, "valueHolder was null");
    }

    @Override
    public SoapContainerContext<T> writeStart() throws TException {
      getValueHolder().writeStart();
      return this;
    }

    @Override
    public SoapContainerContext<T> writeEnd() throws TException {
      getValueHolder().writeEnd();
      return this;
    }

    @Override
    public SoapContainerContext<T> readStart() throws TException {
      return this;
    }

    @Override
    public SoapContainerContext<T> readEnd() throws TException {
      return this;
    }

    @Override
    public final SoapContainerValueHolder getValueHolder() {
      return valueHolder;
    }

    @Override
    public void beforeRead() throws TException {
//      if (!
          getValueHolder().next()
//        ) throw new IllegalStateException("count: " + getValueHolder().count())
          ;
    }

    @Override
    public void beforeWrite() throws TException {

    }

    @Override
    protected int size() throws TException {
      return getValueHolder().size();
    }

    @Override
    protected StructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> structSchema) throws TException {
      return new SoapStructContext(this, structSchema, getValueHolder());
//      if (isReading()) {
//        final JsonValue value = valueHolder.valueToRead();
//        switch (value.getValueType()) {
//        case OBJECT:
//          final JsonValueHolder holder = new JsonValueHolder();
//          holder.setValue(value);
//          return new SoapStructContext(this, structSchema, holder);
//        default:
//          throw ex("Expected JsonObject but was: " + value.getValueType());
//        }
//      } else {
//        return new SoapStructContext(this, structSchema, getValueHolder());
//      }
    }

    @Override
    protected ListContext newList(ListSchemaType listSchema) throws TException {
      final Element element = getValueHolder().createElement();
      return new SoapListContext(
        this,
        listSchema,
        new SoapArrayValueHolder(element, "item")
      );
//      if (isReading()) {
//        final JsonValue value = requireNonNull(valueHolder.valueToRead());
//        switch (value.getValueType()) {
//        case ARRAY:
//          return new JsonListContext(
//            this, listSchema, new JsonArrayValueHolder((JsonArray) value));
//        default:
//          throw ex("Expected JsonArray but was: " + value.getValueType());
//        }
//      } else {
//        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
////        holder.setKey(schema().getName());
//        return new JsonListContext(this, listSchema, holder);
//      }
    }

    @Override
    protected SetContext newSet(SetSchemaType setSchema) throws TException {
      final Element element = getValueHolder().createElement();
      return new SoapSetContext(
        this, setSchema, new SoapArrayValueHolder(element, "item")
      );
//      if (isReading()) {
//        final JsonValue value = requireNonNull(valueHolder.valueToRead());
//        switch (value.getValueType()) {
//        case ARRAY:
//          return new JsonSetContext(
//            this, setSchema, new JsonArrayValueHolder((JsonArray) value));
//        default:
//          throw ex("Expected JsonArray but was: " + value.getValueType());
//        }
//      } else {
//        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
////        holder.setKey(schema().getName());
//        return new JsonSetContext(this, setSchema, holder);
//      }
    }

    @Override
    protected MapContext newMap(MapSchemaType mapSchema) throws TException {
      final Element element = getValueHolder().createElement();
      return new SoapMapContext(
        this, mapSchema, new SoapArrayValueHolder(element, "entry")
      );
//      if (isReading()) {
//        final JsonValue value = requireNonNull(valueHolder.valueToRead());
//        switch (value.getValueType()) {
//        case ARRAY:
//          return new JsonMapContext(
//            this, mapSchema, new JsonArrayValueHolder((JsonArray) value));
//        default:
//          throw ex("Expected JsonArray but was: " + value.getValueType());
//        }
//      } else {
//        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
////        holder.setKey(schema().getName());
//        return new JsonMapContext(this, mapSchema, holder);
//      }
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

    private SoapValueHolder originalValueHolder;

    public SoapMapContext(
        final Context parent,
        final MapSchemaType schema,
        final SoapArrayValueHolder valueHolder) throws TException {
      super(parent, schema, TMap.class, ContainerType.MAP, valueHolder);
      originalValueHolder = new SoapValueHolder(valueHolder.getParentNode());
      originalValueHolder.setKey("entry");
    }

    @Override
    protected SchemaType getSchemaType() throws TException {
      return isKey
        ? ((MapSchemaType) getContainerSchemaType()).getKeyType()
        : getContainerSchemaType().getValueType();
    }

    @Override
    protected int size() throws TException {
      return getValueHolder().size() / 2;
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
    public void beforeRead() throws TException {
      isKey=!isKey;
      super.beforeRead();
    }

    @Override
    public void beforeWrite() throws TException {
      isKey=!isKey;
      if (isKey) {
        final Element element = originalValueHolder.createElement();
        getValueHolder().setParentNode(element);
        getValueHolder().setKey("key");
      } else {
        getValueHolder().setKey("value");
      }
//      getValueHolder().createElement();
      super.beforeWrite();
    }
  }

  protected class SoapValueHolder implements ValueHolder {

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
        newElement = doc.createElement(key);
      }
      parentNode.appendChild(newElement);
      this._element = newElement;
      return this._element;
    }

    protected void write(String str) throws TException {
      // TODO: tmp
//      final Node value = getValue();
//      final String key = getKey();
//      if (value == null) {
//        return;
//      }
//      if (key == null) {
//        return;
//      }
      // end tmp
      createElement().setTextContent(str);
    }

    @Override
    public ByteBuffer readBinary() throws TException {
      return ByteBuffer.wrap(parseBase64Binary(readString()));
    }

    @Override
    public boolean readBool() throws TException {
      throw new UnsupportedOperationException();
      /*
      final JsonValue value = valueToRead();
      switch (value.getValueType()) {
      case TRUE:
        return true;
      case FALSE:
        return false;
      default:
        throw ex("Expected JSON boolean but was " + value.getValueType());
      }
      */
    }

    @Override
    public byte readByte() throws TException {
      return (byte) readI32();
    }

    @Override
    public double readDouble() throws TException {
      throw new UnsupportedOperationException();
      /*
      final JsonValue value = valueToRead();
      if (value instanceof JsonNumber) {
//      switch (value.getValueType()) {
//      case NUMBER:
        return ((JsonNumber)value).doubleValue();
//      default:
      } else {
        throw ex("Expected JSON number but was " + value.getValueType());
      }
      */
    }

    @Override
    public short readI16() throws TException {
      return (short) readI32();
    }

    @Override
    public int readI32() throws TException {
      throw new UnsupportedOperationException();
      /*
      final JsonValue value = valueToRead();
      if (value instanceof JsonNumber) {
//      switch (value.getValueType()) {
//      case NUMBER:
        try {
          return ((JsonNumber)value).intValueExact();
        } catch (ArithmeticException e) {
          throw ex(
            "Expected integral JSON number was actually: " +
              ((JsonNumber)value).toString());
        }
      } else {
//      default:
        throw ex("Expected JSON number but was " + value.getValueType());
      }
      */
    }

    @Override
    public long readI64() throws TException {
      throw new UnsupportedOperationException();
      /*
      final JsonValue value = valueToRead();
      if (value instanceof JsonNumber) {
//      switch (value.getValueType()) {
//      case NUMBER:
        try {
          return ((JsonNumber)value).longValueExact();
        } catch (ArithmeticException e) {
          throw ex(
            "Expected integral JSON number was actually: " +
              ((JsonNumber)value).toString());
        }
      } else {
//      default:
        throw ex("Expected JSON number but was " + value.getValueType());
      }
      */
    }

    @Override
    public String readString() throws TException {
      throw new UnsupportedOperationException();
/*      final JsonValue value = valueToRead();
      if (value instanceof JsonString) {
        return ((JsonString)value).getString();
      }
//      final ValueType type = value.getValueType();
//      switch (type) {
//      case STRING:
//        return ((JsonString)value).getString();
//      default:
        throw ex("Expected JSON string but was " + value.getValueType());
//      }*/
    }

    protected String valueToRead() throws TException {
      throw new UnsupportedOperationException();
//      final JsonValue value = getValue();
//      switch (value.getValueType()) {
//      case OBJECT:
//        final String key = getKey();
//        if (key == null) {
//          throw ex("No key set to read for JsonObject");
//        }
//        final JsonValue result = ((JsonObject)value).get(key);
//        if (result == null) {
//          throw ex("No mapping for key: " + key);
//        }
//        return result;
//      case ARRAY:
//      case NULL:
//        throw ex(new IllegalStateException("Should not be ARRAY or NULL"));
//      default:
//        return value;
//      }
    }

    protected Element lastElementCreated() {
      return this._element;
      /*
      final String key = getKey();
      if (key == null) {
        return null;
//        throw new IllegalStateException();
      }
      for (Node node = getValue().getFirstChild();
            node != null; node = node.getNextSibling()) {
        final String localName = node.getNodeName();
        if (node.getNodeType() == Node.ELEMENT_NODE && localName.equals(key)) {
          return (Element) node;
        }
      }
//      throw new IllegalStateException("no element for key: " + key);
      return null;
      */
    }

    public SoapValueHolder fromLastElement() {
      final SoapValueHolder holder = new SoapValueHolder(lastElementCreated());
      return holder;
    }

  }

  protected abstract class SoapContainerValueHolder extends SoapValueHolder {
    private int _count = 0;

    public SoapContainerValueHolder(Node value) {
      super(value);
    }
    final boolean next() throws TException {
      if (advance()) {
        _count++;
        return true;
      } else {
        return false;
      }
    }
    final int count() {
      return _count;
    }
    protected abstract int size() throws TException;
    protected abstract boolean advance() throws TException;
    protected abstract void writeStart() throws TException;
    protected abstract void writeEnd() throws TException;
  }

  protected class SoapArrayValueHolder extends SoapContainerValueHolder {
//    private Element lastElement;
    public SoapArrayValueHolder(Node parentNode, String key) {
      super(parentNode);
      setKey(key);
    }
    @Override
    protected int size() throws TException {
//      try {
//        return this.jsonArray.size();
//      } catch (NullPointerException e) {
//        return -1;
//      }
      return -1;
    }
    @Override
    protected boolean advance() throws TException {
//      return (count()) < this.jsonArray.size();
      return false;
    }
    @Override
    protected String valueToRead() throws TException {
      throw new UnsupportedOperationException();
//      final int index = count() - 1;
//      if (index < 0) {
//        throw ex("index is less than 0; has next() been called at least once?");
//      }
//      final JsonValue result = this.jsonArray.get(index);
//      return result;
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
//      TSOAPProtocol.this.writeEnd();
    }
  }

//  protected class JsonObjectValueHolder extends JsonContainerValueHolder {
//    private final Iterator<String> iterator;
//    public JsonObjectValueHolder(final JsonObject object) {
//      setValue(requireNonNull(object, "jsonObject was null"));
//      this.iterator = object.keySet().iterator();
//    }
//    @Override
//    protected int size() throws TException {
//      return ((JsonObject) getValue()).size();
//    }
//    @Override
//    protected boolean advance() throws TException {
//      if (this.iterator.hasNext()) {
//        setKey(this.iterator.next());
//        return true;
//      } else {
//        setKey(null);
//        return false;
//      }
//    }
//    @Override
//    protected JsonValue valueToRead() throws TException {
//      return super.valueToRead();
//    }
//  }

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
    final InputStream in = new TTransportInputStream(getTransport());
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
    try {
      final DocumentBuilder b = documentBuilderFactory.newDocumentBuilder();
      final Document doc = b.parse(new TTransportInputStream(getTransport()));
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
//
//  private static String expectString(JsonObject json, String key)
//      throws TException {
//    return ((JsonString) expectValue(json, key, ValueType.STRING)).getString();
//  }
//
//  private static int expectInt(JsonObject json, String key)
//      throws TException {
//    final JsonValue val = expectValue(json, key, ValueType.NUMBER);
//    try {
//      return ((JsonNumber) val).intValueExact();
//    } catch (ArithmeticException e) {
//      throw ex("Expected exact int value", e);
//    }
//  }
//
//  private static JsonObject expectObject(JsonObject json, String key)
//      throws TException {
//    return (JsonObject) expectValue(json, key, ValueType.OBJECT);
//  }
//
//  private static JsonValue expectValue(
//      JsonObject json, String key, ValueType type) throws TException {
//    final JsonValue val = expectValue(json, key);
//    final boolean matches;
//    switch (type) {
//    case STRING:
//      matches = (val instanceof JsonString); break;
//    case NUMBER:
//      matches = (val instanceof JsonNumber); break;
//    default:
//      matches = val.getValueType().equals(type); break;
//    }
//    if (!matches) {
//      throw ex("Expected " + type + " but was actually: " + val.getClass());
//    }
//    return val;
//  }
//
//  private static JsonValue expectValue(JsonObject json, String key)
//      throws TException {
//    final JsonValue val = json.get(key);
//    if (val == null) {
//      throw ex("Expected value for key: " + key);
//    }
//    return val;
//  }

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

}
