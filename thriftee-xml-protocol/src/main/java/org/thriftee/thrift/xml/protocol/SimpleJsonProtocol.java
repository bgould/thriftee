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
package org.thriftee.thrift.xml.protocol;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.requireNonNull;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerator;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.transport.TTransport;
import org.thriftee.compiler.schema.AbstractFieldSchema;
import org.thriftee.compiler.schema.AbstractStructSchema;
import org.thriftee.compiler.schema.ContainerSchemaType;
import org.thriftee.compiler.schema.ListSchemaType;
import org.thriftee.compiler.schema.MapSchemaType;
import org.thriftee.compiler.schema.SchemaType;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.schema.SetSchemaType;
import org.thriftee.compiler.schema.StructSchema;


public class SimpleJsonProtocol extends AbstractSimpleProtocol {

  /**
   * Factory
   */
  public static class Factory extends AbstractFactory<SimpleJsonProtocol> {

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
    protected SimpleJsonProtocol getProtocol(
        final TTransport trans,
        final ServiceSchema baseService,
        final AbstractStructSchema<?,?,?,?> baseStruct) {
      return new SimpleJsonProtocol(trans, baseService, baseStruct);
    }

  }

  protected class JsonBaseContext extends AbstractSimpleBaseContext {

    private JsonObject __parsed;

    protected JsonBaseContext(ContextType type,
        ServiceSchema baseService,
        AbstractStructSchema<?, ?, ?, ?> baseStruct) {
      super(type, baseService, baseStruct);
    }

    @Override
    protected JsonMessageContext newMessage(ServiceSchema schema)
        throws TException {
      __parsed = isReading() ? parse() : null;
      return new JsonMessageContext(this, getBaseService(), __parsed);
    }

    @Override
    protected JsonStructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
      __parsed = isReading() ? parse() : null;
      final JsonValueHolder holder = new JsonValueHolder();
      holder.setValue(__parsed);
      return new JsonStructContext(this, getBaseStruct(), holder);
    }

    @Override
    public void popped() throws TException {
      __parsed = null;
      if (__writer != null) {
        try {
          __writer.flush();
        } catch (Exception e) {
          throw ex(e);
        } finally {
          try {
            __writer.close();
          } catch (Exception e) {
            LOG.warn("Error closing writer", e);
          } finally {
            __writer = null;
          }
        }
      }
    }

    @Override
    protected ServiceSchema getBaseService() {
      return SimpleJsonProtocol.this.getBaseService();
    }

    @Override
    protected AbstractStructSchema<?, ?, ?, ?> getBaseStruct() {
      return SimpleJsonProtocol.this.getBaseStruct();
    }

  }

  protected class JsonMessageContext extends AbstractSimpleMessageContext {

    private final JsonObject json;

    public JsonMessageContext(
        Context parent, ServiceSchema svc, JsonObject json) {
      super(parent, svc);
      this.json = json;
    }

    @Override
    public JsonMessageContext writeStart() throws TException {
      writeStartObject(); // start message object
      writeAttribute(ATTRIBUTE_TYPE, byteToMessageType(messageType()));
      writeAttribute(ATTRIBUTE_NAME, name());
      writeAttribute(ATTRIBUTE_SEQID, seqid());
      return this;
    }

    @Override
    public JsonMessageContext readStart() throws TException {
      if (json == null) throw ex("no json in " + type() + " context");
      set(
        expectString(json, ATTRIBUTE_NAME),
        messageTypeToByte(expectString(json, ATTRIBUTE_TYPE)),
        json.containsKey(ATTRIBUTE_SEQID) ? expectInt(json, ATTRIBUTE_SEQID) : 1
      );
      return this;
    }

    @Override
    public JsonMessageContext writeEnd() throws TException {
      SimpleJsonProtocol.this.writeEnd(); // end message object
      return this;
    }

    @Override
    public JsonMessageContext readEnd() throws TException {
      return this;
    }

    @Override
    public StructContext newStruct(
            final AbstractStructSchema<?, ?, ?, ?> schema) throws TException {
      final JsonValueHolder holder = new JsonValueHolder();
      holder.setValue(isReading() ? expectObject(json, ATTRIBUTE_ARGS) : null);
      holder.setKey(ATTRIBUTE_ARGS);
      return new JsonStructContext(this, schema, holder);
    }

    @Override
    public String toString() {
      return "<TMessage name:'"+name()+"' "
            + "type: "+messageType()+" seqid:"+seqid()+">";
    }

  }

  protected class JsonStructContext
        extends AbstractSimpleStructContext<JsonFieldContext> {

    private final JsonValueHolder valueHolder;

    private Iterator<String> fieldIterator;

    public JsonStructContext(
        final Context parent,
        final AbstractStructSchema<?, ?, ?, ?> structSchema,
        final JsonValueHolder holder) {
      super(parent, structSchema);
      this.valueHolder = requireNonNull(holder, "struct valueHolder was null");
      if (isReading()) {
        requireNonNull((JsonObject) this.valueHolder.getValue());
      }
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
      if (valueHolder.getKey() != null) {
        writeStartObject(valueHolder.getKey());
      } else {
        writeStartObject();
      }
      return this;
    }

    @Override
    public StructContext writeEnd() throws TException {
      SimpleJsonProtocol.this.writeEnd();
      return this;
    }

    @Override
    public StructContext writeFieldStop() throws TException {
      return this;
    }

    @Override
    public StructContext readStart() throws TException {
      this.fieldIterator =
        ((JsonObject) this.valueHolder.getValue()).keySet().iterator(); //schema().getFields().keySet().iterator();
      return this;
    }

    @Override
    public StructContext readEnd() throws TException {
      this.fieldIterator = null;
      return this;
    }

    @Override
    protected JsonFieldContext newField(AbstractFieldSchema<?, ?> schema)
        throws TException {
      if (isReading()) {
        final JsonObject json = (JsonObject) valueHolder.getValue();
        if (json == null) {
          throw ex("JsonValue should not be null for " + schema);
        }
//        final JsonValue val = json.get(schema.getName());
//        if (val == null) {
//          throw ex("JsonValue should not be null for " + schema);
//        }
        final JsonValueHolder holder = new JsonValueHolder();
        holder.setKey(schema.getName());
        holder.setValue(json);
//        holder.setValue(val);
        return new JsonFieldContext(this, schema, holder);
      } else {
        final JsonValueHolder holder = new JsonValueHolder();
        return new JsonFieldContext(this, null, holder);
      }
    }

  }

  protected class JsonFieldContext
      extends AbstractSimpleFieldContext
      implements FieldContext {

    private final JsonValueHolder valueHolder;

    public JsonFieldContext(
        final JsonStructContext parent,
        final AbstractFieldSchema<?, ?> fieldSchema,
        final JsonValueHolder valueHolder) {
      super(parent, fieldSchema);
      this.valueHolder = requireNonNull(valueHolder);
    }

    @Override
    public JsonFieldContext writeStart() throws TException {
      return this;
    }

    @Override
    public JsonFieldContext writeEnd() throws TException {
      return this;
    }

    @Override
    public JsonFieldContext readStart() throws TException {
      return this;
    }

    @Override
    public JsonFieldContext readEnd() throws TException {
      return this;
    }

    @Override
    protected JsonValueHolder getValueHolder() {
      if (isWriting()) {
        valueHolder.setKey(schema().getName());
      }
      return valueHolder;
    }

    @Override
    protected StructContext newStruct(
        AbstractStructSchema<?, ?, ?, ?> structSchema) throws TException {
      if (isReading()) {
        final JsonValue value = valueHolder.valueToRead();
        switch (value.getValueType()) {
        case OBJECT:
          final JsonValueHolder holder = new JsonValueHolder();
          holder.setValue(value);
          return new JsonStructContext(this, structSchema, holder);
        default:
          throw ex("Expected JsonObject but was: " + value.getValueType());
        }
      } else {
        return new JsonStructContext(this, structSchema, getValueHolder());
      }
    }

    @Override
    protected ListContext newList(ListSchemaType listSchema) throws TException {
      if (isReading()) {
        final JsonValue value = valueHolder.valueToRead();
        switch (value.getValueType()) {
        case ARRAY:
          return new JsonListContext(
            this, listSchema, new JsonArrayValueHolder((JsonArray) value));
        default:
          throw ex("Expected JsonArray but was: " + value.getValueType());
        }
      } else {
        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
        holder.setKey(schema().getName());
        return new JsonListContext(this, listSchema, holder);
      }
    }

    @Override
    protected SetContext newSet(SetSchemaType setSchema) throws TException {
      if (isReading()) {
        final JsonValue value = valueHolder.valueToRead();
        switch (value.getValueType()) {
        case ARRAY:
          return new JsonSetContext(
            this, setSchema, new JsonArrayValueHolder((JsonArray) value));
        default:
          throw ex("Expected JsonArray but was: " + value.getValueType());
        }
      } else {
        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
        holder.setKey(schema().getName());
        return new JsonSetContext(this, setSchema, holder);
      }
    }

    @Override
    protected MapContext newMap(MapSchemaType mapSchema) throws TException {
      if (isReading()) {
        final JsonValue value = valueHolder.valueToRead();
        switch (value.getValueType()) {
        case ARRAY:
          return new JsonMapContext(
            this, mapSchema, new JsonArrayValueHolder((JsonArray) value));
        default:
          throw ex("Expected JsonArray but was: " + value.getValueType());
        }
      } else {
        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
        holder.setKey(schema().getName());
        return new JsonMapContext(this, mapSchema, holder);
      }
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

  protected abstract class JsonContainerContext<T>
      extends AbstractSimpleContainerContext<T> {

    private JsonContainerValueHolder valueHolder;

    protected JsonContainerContext(
        final Context parent,
        final ContainerSchemaType schemaType,
        final Class<T> emitType,
        final ContainerType containerType,
        final JsonContainerValueHolder valueHolder) {
      super(parent, schemaType, emitType, containerType);
      this.valueHolder = requireNonNull(valueHolder, "valueHolder was null");
    }

    @Override
    public JsonContainerContext<T> writeStart() throws TException {
      getValueHolder().writeStart();
      return this;
    }

    @Override
    public JsonContainerContext<T> writeEnd() throws TException {
      getValueHolder().writeEnd();
      return this;
    }

    @Override
    public JsonContainerContext<T> readStart() throws TException {
      return this;
    }

    @Override
    public JsonContainerContext<T> readEnd() throws TException {
      return this;
    }

    @Override
    public final JsonContainerValueHolder getValueHolder() {
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
      if (isReading()) {
        final JsonValue value = valueHolder.valueToRead();
        switch (value.getValueType()) {
        case OBJECT:
          final JsonValueHolder holder = new JsonValueHolder();
          holder.setValue(value);
          return new JsonStructContext(this, structSchema, holder);
        default:
          throw ex("Expected JsonObject but was: " + value.getValueType());
        }
      } else {
        return new JsonStructContext(this, structSchema, getValueHolder());
      }
    }

    @Override
    protected ListContext newList(ListSchemaType listSchema) throws TException {
      if (isReading()) {
        final JsonValue value = requireNonNull(valueHolder.valueToRead());
        switch (value.getValueType()) {
        case ARRAY:
          return new JsonListContext(
            this, listSchema, new JsonArrayValueHolder((JsonArray) value));
        default:
          throw ex("Expected JsonArray but was: " + value.getValueType());
        }
      } else {
        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
//        holder.setKey(schema().getName());
        return new JsonListContext(this, listSchema, holder);
      }
    }

    @Override
    protected SetContext newSet(SetSchemaType setSchema) throws TException {
      if (isReading()) {
        final JsonValue value = requireNonNull(valueHolder.valueToRead());
        switch (value.getValueType()) {
        case ARRAY:
          return new JsonSetContext(
            this, setSchema, new JsonArrayValueHolder((JsonArray) value));
        default:
          throw ex("Expected JsonArray but was: " + value.getValueType());
        }
      } else {
        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
//        holder.setKey(schema().getName());
        return new JsonSetContext(this, setSchema, holder);
      }
    }

    @Override
    protected MapContext newMap(MapSchemaType mapSchema) throws TException {
      if (isReading()) {
        final JsonValue value = requireNonNull(valueHolder.valueToRead());
        switch (value.getValueType()) {
        case ARRAY:
          return new JsonMapContext(
            this, mapSchema, new JsonArrayValueHolder((JsonArray) value));
        default:
          throw ex("Expected JsonArray but was: " + value.getValueType());
        }
      } else {
        final JsonArrayValueHolder holder = new JsonArrayValueHolder(null);
//        holder.setKey(schema().getName());
        return new JsonMapContext(this, mapSchema, holder);
      }
    }

  }

  protected class JsonListContext
      extends JsonContainerContext<TList>
      implements ListContext {

    public JsonListContext(
        final Context parent,
        final ListSchemaType schema,
        final JsonArrayValueHolder valueHolder) throws TException {
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

  protected class JsonSetContext
      extends JsonContainerContext<TSet>
      implements SetContext {

    public JsonSetContext(
        final Context parent,
        final SetSchemaType schema,
        final JsonArrayValueHolder valueHolder) throws TException {
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

  protected class JsonMapContext
      extends JsonContainerContext<TMap>
      implements MapContext {

    protected boolean isKey = false;

    public JsonMapContext(
        final Context parent,
        final MapSchemaType schema,
        final JsonArrayValueHolder valueHolder) throws TException {
      super(parent, schema, TMap.class, ContainerType.MAP, valueHolder);
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
      super.beforeWrite();
    }
  }

  protected class JsonValueHolder implements ValueHolder {

    private JsonValue _value;

    private String _key;

    public void setValue(JsonValue value) {
      this._value = value;
    }

    public JsonValue getValue() {
      return this._value;
    }

    public String getKey() {
      return _key;
    }

    public void setKey(String key) {
      this._key = key;
    }

    protected JsonValue value() {
      return requireNonNull(getValue(), "JsonValue should not be null");
    }

    @Override
    public void writeBinary(ByteBuffer buffer) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(printBase64Binary(buffer.array()));
      } else {
        writer().write(key, printBase64Binary(buffer.array()));
      }
    }

    @Override
    public void writeBool(boolean bool) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(bool);
      } else {
        writer().write(key, bool);
      }
    }

    @Override
    public void writeByte(byte bite) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(bite);
      } else {
        writer().write(key, bite);
      }
    }

    @Override
    public void writeDouble(double dbl) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(dbl);
      } else {
        writer().write(key, dbl);
      }
    }

    @Override
    public void writeI16(short i16) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(i16);
      } else {
        writer().write(key, i16);
      }
    }

    @Override
    public void writeI32(int i32) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(i32);
      } else {
        writer().write(key, i32);
      }
    }

    @Override
    public void writeI64(long i64) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(i64);
      } else {
        writer().write(key, i64);
      }
    }

    @Override
    public void writeString(String str) throws TException {
      final String key = getKey();
      if (key == null) {
        writer().write(str);
      } else {
        writer().write(key, str);
      }
    }

    @Override
    public ByteBuffer readBinary() throws TException {
      return ByteBuffer.wrap(parseBase64Binary(readString()));
    }

    @Override
    public boolean readBool() throws TException {
      final JsonValue value = valueToRead();
      switch (value.getValueType()) {
      case TRUE:
        return true;
      case FALSE:
        return false;
      default:
        throw ex("Expected JSON boolean but was " + value.getValueType());
      }
    }

    @Override
    public byte readByte() throws TException {
      return (byte) readI32();
    }

    @Override
    public double readDouble() throws TException {
      final JsonValue value = valueToRead();
      if (value instanceof JsonNumber) {
//      switch (value.getValueType()) {
//      case NUMBER:
        return ((JsonNumber)value).doubleValue();
//      default:
      } else {
        throw ex("Expected JSON number but was " + value.getValueType());
      }
    }

    @Override
    public short readI16() throws TException {
      return (short) readI32();
    }

    @Override
    public int readI32() throws TException {
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
    }

    @Override
    public long readI64() throws TException {
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
    }

    @Override
    public String readString() throws TException {
      final JsonValue value = valueToRead();
      if (value instanceof JsonString) {
        return ((JsonString)value).getString();
      }
//      final ValueType type = value.getValueType();
//      switch (type) {
//      case STRING:
//        return ((JsonString)value).getString();
//      default:
        throw ex("Expected JSON string but was " + value.getValueType());
//      }
    }

    protected JsonValue valueToRead() throws TException {
      final JsonValue value = getValue();
      switch (value.getValueType()) {
      case OBJECT:
        final String key = getKey();
        if (key == null) {
          throw ex("No key set to read for JsonObject");
        }
        final JsonValue result = ((JsonObject)value).get(key);
        if (result == null) {
          throw ex("No mapping for key: " + key);
        }
        return result;
      case ARRAY:
      case NULL:
        throw ex(new IllegalStateException("Should not be ARRAY or NULL"));
      default:
        return value;
      }
    }

  }

  protected abstract class JsonContainerValueHolder extends JsonValueHolder {
    private int _count = 0;
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

  protected class JsonArrayValueHolder extends JsonContainerValueHolder {
    private final JsonArray jsonArray;
    public JsonArrayValueHolder(JsonArray array) {
      this.jsonArray = array; //requireNonNull(array, "jsonArray was null");
    }
    @Override
    protected int size() throws TException {
      try {
        return this.jsonArray.size();
      } catch (NullPointerException e) {
        return -1;
      }
    }
    @Override
    protected boolean advance() throws TException {
      return (count()) < this.jsonArray.size();
    }
    @Override
    protected JsonValue valueToRead() throws TException {
      final int index = count() - 1;
      if (index < 0) {
        throw ex("index is less than 0; has next() been called at least once?");
      }
      final JsonValue result = this.jsonArray.get(index);
      return result;
    }
    @Override
    protected void writeStart() throws TException {
      if (getKey() != null) {
        writeStartArray(getKey());
        setKey(null);
      } else {
        writeStartArray();
      }
    }
    @Override
    protected void writeEnd() throws TException {
      SimpleJsonProtocol.this.writeEnd();
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
    return new JsonBaseContext(type, getBaseService(), getBaseStruct());
  }

  protected SimpleJsonProtocol(
      final TTransport trans,
      final ServiceSchema baseService,
      final AbstractStructSchema<?,?,?,?> baseStruct) {
    super(trans, baseService, baseStruct);
  }

  private JsonGenerator __writer;

  protected JsonGenerator writer() throws TException {
    if (__writer == null) {
      __writer = Json.createGenerator(
        new TTransportOutputStream(getTransport())
      );
    }
    return __writer;
  }

  protected JsonObject parse() throws TException {
    JsonReader reader = null;
    try {
      reader = Json.createReader(new TTransportInputStream(getTransport()));
      return reader.readObject();
    } catch (Exception e) {
      throw ex(e);
    } finally {
      try {
        reader.close();
      } catch (Throwable t) {}
    }
  }

  protected final void writeStartObject() throws TException {
    try {
      writer().writeStartObject();
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  protected final void writeStartArray() throws TException {
    try {
      writer().writeStartArray();
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  protected final void writeStartObject(String name) throws TException {
    try {
      writer().writeStartObject(name);
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  protected final void writeStartArray(String name) throws TException {
    try {
      writer().writeStartArray(name);
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  protected final void writeAttribute(String name, String s) throws TException {
    try {
      writer().write(name, s);
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  protected final void writeAttribute(String name, int s) throws TException {
    try {
      writer().write(name, s);
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  protected void writeEnd() throws TException {
    try {
      writer().writeEnd();
    } catch (JsonException e) {
      throw ex(e);
    }
  }

  private static String expectString(JsonObject json, String key)
      throws TException {
    return ((JsonString) expectValue(json, key, ValueType.STRING)).getString();
  }

  private static int expectInt(JsonObject json, String key)
      throws TException {
    final JsonValue val = expectValue(json, key, ValueType.NUMBER);
    try {
      return ((JsonNumber) val).intValueExact();
    } catch (ArithmeticException e) {
      throw ex("Expected exact int value", e);
    }
  }

  private static JsonObject expectObject(JsonObject json, String key)
      throws TException {
    return (JsonObject) expectValue(json, key, ValueType.OBJECT);
  }

  private static JsonValue expectValue(
      JsonObject json, String key, ValueType type) throws TException {
    final JsonValue val = expectValue(json, key);
    final boolean matches;
    switch (type) {
    case STRING:
      matches = (val instanceof JsonString); break;
    case NUMBER:
      matches = (val instanceof JsonNumber); break;
    default:
      matches = val.getValueType().equals(type); break;
    }
    if (!matches) {
      throw ex("Expected " + type + " but was actually: " + val.getClass());
    }
    return val;
  }

  private static JsonValue expectValue(JsonObject json, String key)
      throws TException {
    final JsonValue val = json.get(key);
    if (val == null) {
      throw ex("Expected value for key: " + key);
    }
    return val;
  }

  private static final String ATTRIBUTE_TYPE =       "type";
  private static final String ATTRIBUTE_NAME =       "name";
  private static final String ATTRIBUTE_SEQID =      "seqid";
  private static final String ATTRIBUTE_ARGS =       "args";

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
