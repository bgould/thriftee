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

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.transport.TTransport;
import org.thriftee.thrift.schema.AbstractFieldSchema;
import org.thriftee.thrift.schema.AbstractStructSchema;
import org.thriftee.thrift.schema.ContainerSchemaType;
import org.thriftee.thrift.schema.ListSchemaType;
import org.thriftee.thrift.schema.MapSchemaType;
import org.thriftee.thrift.schema.MethodSchema;
import org.thriftee.thrift.schema.SchemaException;
import org.thriftee.thrift.schema.SchemaType;
import org.thriftee.thrift.schema.ServiceSchema;
import org.thriftee.thrift.schema.SetSchemaType;
import org.thriftee.thrift.schema.StructSchema;
import org.thriftee.thrift.schema.UnionSchema;
import org.thriftee.thrift.xml.protocol.TJsonApiProtocol.JsonFieldContext;

public abstract class AbstractSimpleProtocol extends AbstractContextProtocol {

  /**
   * Factory
   */
  public abstract static class AbstractFactory<T extends AbstractSimpleProtocol>
      implements TProtocolFactory {

    private static final long serialVersionUID = -2988163176565419085L;

    private ServiceSchema baseService;

    private AbstractStructSchema<?, ?, ?, ?> baseStruct;

    public AbstractFactory() {
      this(null, null);
    }

    public AbstractFactory(ServiceSchema baseService) {
      this(baseService, null);
    }

    public AbstractFactory(StructSchema baseStruct) {
      this(null, baseStruct);
    }

    public AbstractFactory(
        final ServiceSchema baseService,
        final AbstractStructSchema<?, ?, ?, ?> baseStruct) {
      super();
      this.baseService = baseService;
      this.baseStruct = baseStruct;
    }

    @Override
    public T getProtocol(TTransport trans) {
      return getProtocol(trans, baseService, baseStruct);
    }

    protected abstract T getProtocol(
        TTransport trans,
        ServiceSchema baseService,
        AbstractStructSchema<?, ?, ?, ?> baseStruct);

    public ServiceSchema getBaseService() {
      return baseService;
    }

    public void setBaseService(ServiceSchema baseService) {
      this.baseService = baseService;
    }

    public AbstractStructSchema<?, ?, ?, ?> getBaseStruct() {
      return baseStruct;
    }

    public void setBaseStruct(AbstractStructSchema<?, ?, ?, ?> baseStruct) {
      this.baseStruct = baseStruct;
    }

  }

  protected AbstractSimpleProtocol(
      TTransport trans,
      ServiceSchema baseService,
      AbstractStructSchema<?, ?, ?, ?> baseStruct) {
    super(trans);
    this.baseService = baseService;
    this.baseStruct = baseStruct;
  }

  private ServiceSchema baseService;

  private AbstractStructSchema<?, ?, ?, ?> baseStruct;

  public ServiceSchema getBaseService() {
    return baseService;
  }

  public void setBaseService(ServiceSchema baseService) {
    this.baseService = baseService;
  }

  public AbstractStructSchema<?, ?, ?, ?> getBaseStruct() {
    return baseStruct;
  }

  public void setBaseStruct(AbstractStructSchema<?, ?, ?, ?> baseStruct) {
    this.baseStruct = baseStruct;
  }

  public static abstract class AbstractSimpleBaseContext extends BaseContext {

    protected AbstractSimpleBaseContext(
        ContextType type,
        ServiceSchema baseService,
        AbstractStructSchema<?, ?, ?, ?> baseStruct) {
      super(type);
    }

    @Override
    public final MessageContext newMessage() throws TException {
      final ServiceSchema baseService = getBaseService();
      if (baseService == null) {
        throw new TProtocolException("A service schema has not been set.");
      }
      return newMessage(baseService);
    }

    protected abstract MessageContext newMessage(ServiceSchema schema)
        throws TException;

    @Override
    public final StructContext newStruct() throws TException {
      final AbstractStructSchema<?, ?, ?, ?> baseStruct = getBaseStruct();
      if (baseStruct == null) {
        throw new TProtocolException("A struct schema has not been set.");
      }
      return newStruct(baseStruct);
    }

    protected abstract StructContext
        newStruct(AbstractStructSchema<?, ?, ?, ?> schema) throws TException;

    protected abstract ServiceSchema getBaseService();

    protected abstract AbstractStructSchema<?, ?, ?, ?> getBaseStruct();

  }

  public static abstract class AbstractSimpleMessageContext
      extends AbstractContext
      implements MessageContext {

    protected final ServiceSchema svc;

//    private Str method;
    private String name;
    private byte type;
    private int seqid;

    protected AbstractSimpleMessageContext(Context parent, ServiceSchema svc) {
      super(parent);
      this.svc = svc;
    }

    protected final String name() {
      return name;
    }
//    protected final MethodSchema method() {
//      return this.method;
//    }

    protected final byte messageType() {
      return type;
    }

    protected final int seqid() {
      return this.seqid;
    }

    @Override
    public final TMessage emit() {
      return new TMessage(name(), messageType(), seqid());
    }

    @Override
    public final void set(TMessage msg) throws TException {
//      try {
//        this.method = svc.findMethod(msg.name);
//      } catch (SchemaException e) {
//        throw ex("Schema error for method named '"+msg.name+"' on "+svc, e);
//      }
      set(msg.name, msg.type, msg.seqid);
    }

    public final void set(String name, byte type, int seqid) {
      this.name = name;
      this.type = type;
      this.seqid = seqid;
    }
    protected abstract StructContext
        newStruct(AbstractStructSchema<?, ?, ?, ?> schema) throws TException;

    @Override
    public final StructContext newStruct() throws TException {
      try {
        final MethodSchema method = svc.findMethod(name());
        final AbstractStructSchema<?, ?, ?, ?> schema;
        switch (type) {
        case TMessageType.CALL:
          schema = method.getArgumentStruct(); break;
        case TMessageType.REPLY:
          schema = method.getResultStruct(); break;
        case TMessageType.EXCEPTION:
          throw new UnsupportedOperationException(
            "need to make a schema for TApplicationException");
        case TMessageType.ONEWAY:
          schema = method.getArgumentStruct(); break;
        default:
          throw ex("Unknown message type: " + Integer.toString(type, 16));
        }
        return newStruct(schema);
      } catch (SchemaException e) {
        throw ex(e);
      }
    }

//    @Override
//    public String toString() {
//      return "<TMessage name:'"+name+"' type: "+type+" seqid:"+seqid+">";
//    }

  }

  public static abstract class AbstractSimpleStructContext<F extends JsonFieldContext>
          extends AbstractStructContext implements StructContext {

    private AbstractStructSchema
        <?, ?, ? extends AbstractFieldSchema<?, ?>, ?> schema;

    private int fieldCount = 0;

    public AbstractSimpleStructContext(
        final Context parent,
        final AbstractStructSchema<?, ?, ?, ?> structSchema) {
      super(parent);
      this.schema = structSchema;
    }

    protected abstract AbstractFieldSchema<?, ?> nextFieldSchema()
        throws TException;

    @Override
    public final F newField() throws TException {
      if (this.schema instanceof UnionSchema && fieldCount > 0) {
        return null;
      }
      if (isReading()) {
        AbstractFieldSchema<?, ?> next = nextFieldSchema();
        if (next == null) {
          return null; // stop field
        } else {
          return newField(next);
        }
      } else {
        return newField(null);
      }
    }

    protected AbstractStructSchema<?, ?, ?, ?> schema() {
      return this.schema;
    }

    protected abstract
    F newField(AbstractFieldSchema<?, ?> schema) throws TException;

  }

  public static abstract class AbstractSimpleFieldContext
      extends AbstractSimpleValueHolderContext
      implements FieldContext {

    private AbstractFieldSchema<?, ?> schema;
//    protected String name;
//    protected byte type;
//    protected short id;

    public AbstractSimpleFieldContext(
        final AbstractSimpleStructContext<?> parent,
        final AbstractFieldSchema<?, ?> fieldSchema) {
      super(parent);
      this.schema = isReading()
                  ? requireNonNull(fieldSchema, "fieldSchema cannot be null.")
                  : null;
    }

    @Override
    public final byte fieldType() {
      return schema().getType().getProtocolType().getType();
//      return this.type;
    }

    protected AbstractSimpleStructContext<?> structctx() {
      return ((AbstractSimpleStructContext<?>)parent());
    }

    @Override
    public final void set(TField field) throws TException {
      AbstractFieldSchema<?, ?> schema = structctx().schema().getField(field.id);
      if (schema == null) {
        throw ex(
          "Field schema not found for id: " + field.id + " : " +
          structctx().schema().getFields()
        );
      }
      this.schema = schema;
//      this.name = field.name;
//      this.type = field.type;
//      this.id = field.id;
    }

    @Override
    public final TField emit() {
      return new TField(
        schema().getName(),
        schema().getType().getProtocolType().getType(),
        schema().getIdentifier().shortValue()
      );
    }

    @Override
    public String toString() {
      return emit().toString();
    }

    protected AbstractFieldSchema<?, ?> schema() {
      return this.schema;
    }
  }

  public static abstract class AbstractSimpleValueHolderContext
      extends AbstractContext
      implements ValueHolderContext {

    AbstractSimpleValueHolderContext(final Context parent) {
      super(requireNonNull(parent, "parent context cannot be null."));
    }

    @Override
    public final void writeBinary(ByteBuffer buffer) throws TException {
      beforeWrite();
      getValueHolder().writeBinary(buffer);
    }

    @Override
    public final void writeBool(boolean bool) throws TException {
      beforeWrite();
      getValueHolder().writeBool(bool);
    }

    @Override
    public final void writeByte(byte bite) throws TException {
      beforeWrite();
      getValueHolder().writeByte(bite);
    }

    @Override
    public final void writeDouble(double dbl) throws TException {
      beforeWrite();
      getValueHolder().writeDouble(dbl);
    }

    @Override
    public final void writeI16(short i16) throws TException {
      beforeWrite();
      getValueHolder().writeI16(i16);
    }

    @Override
    public final void writeI32(int i32) throws TException {
      beforeWrite();
      getValueHolder().writeI32(i32);
    }

    @Override
    public final void writeI64(long i64) throws TException {
      beforeWrite();
      getValueHolder().writeI64(i64);
    }

    @Override
    public final void writeString(String str) throws TException {
      beforeWrite();
      getValueHolder().writeString(str);
    }

    @Override
    public final ByteBuffer readBinary() throws TException {
      beforeRead();
      return getValueHolder().readBinary();
    }

    @Override
    public final boolean readBool() throws TException {
      beforeRead();
      return getValueHolder().readBool();
    }

    @Override
    public final byte readByte() throws TException {
      beforeRead();
      return getValueHolder().readByte();
    }

    @Override
    public final double readDouble() throws TException {
      beforeRead();
      return getValueHolder().readDouble();
    }

    @Override
    public final short readI16() throws TException {
      beforeRead();
      return getValueHolder().readI16();
    }

    @Override
    public final int readI32() throws TException {
      beforeRead();
      return getValueHolder().readI32();
    }

    @Override
    public final long readI64() throws TException {
      beforeRead();
      return getValueHolder().readI64();
    }

    @Override
    public final String readString() throws TException {
      beforeRead();
      return getValueHolder().readString();
    }

    @Override
    public final StructContext newStruct() throws TException {
      if (isReading()) beforeRead(); else beforeWrite();
      try {
        return newStruct(getSchemaType().castTo(AbstractStructSchema.class));
      } catch (ClassCastException e) {
        throw ex("newStruct() called for wrong schema type: " + e.getMessage());
      }
    }

    @Override
    public final ListContext newList() throws TException {
      if (isReading()) beforeRead(); else beforeWrite();
      try {
        return newList(getSchemaType().castTo(ListSchemaType.class));
      } catch (ClassCastException e) {
        throw ex("newList() called for wrong schema type: " + e.getMessage());
      }
    }

    @Override
    public final SetContext newSet() throws TException {
      if (isReading()) beforeRead(); else beforeWrite();
      try {
        return newSet(getSchemaType().castTo(SetSchemaType.class));
      } catch (ClassCastException e) {
        throw ex("newSet() called for wrong schema type: " + e.getMessage());
      }
    }

    @Override
    public final MapContext newMap() throws TException {
      if (isReading()) beforeRead(); else beforeWrite();
      try {
        return newMap(getSchemaType().castTo(MapSchemaType.class));
      } catch (ClassCastException e) {
        throw ex("newMap() called for wrong schema type: " + e.getMessage());
      }
    }

    protected abstract ValueHolder getValueHolder() throws TException;

    protected abstract SchemaType getSchemaType() throws TException;

    protected abstract
      StructContext newStruct(AbstractStructSchema<?, ?, ?, ?> structSchema)
        throws TException;

    protected abstract ListContext newList(ListSchemaType listSchema)
        throws TException;

    protected abstract SetContext newSet(SetSchemaType setSchema)
        throws TException;

    protected abstract MapContext newMap(MapSchemaType mapSchema)
        throws TException;

    protected abstract void beforeRead() throws TException;

    protected abstract void beforeWrite() throws TException;

  }

  public abstract class AbstractSimpleContainerContext<T>
      extends AbstractSimpleValueHolderContext
      implements ContainerContext<T> {

    private final ContainerSchemaType schemaType;
    protected final Class<T> emitType;
    protected final ContainerType containerType;

    protected AbstractSimpleContainerContext(
        final Context parent,
        final ContainerSchemaType schemaType,
        final Class<T> emitType,
        final ContainerType containerType) {
      super(parent);
      this.emitType = requireNonNull(emitType, "emit type cannot be null");
      this.schemaType = requireNonNull(schemaType, "schema type was null");
      this.containerType = requireNonNull(containerType, "container type");
      final Class<?> expectedEmitType;
      switch (containerType) {
      case LIST:
        expectedEmitType = TList.class; break;
      case SET:
        expectedEmitType = TSet.class;  break;
      case MAP:
        expectedEmitType = TMap.class;  break;
      default:
        throw new IllegalStateException();
      }
      if (!expectedEmitType.equals(emitType)) {
        throw new IllegalArgumentException("invalid emit type: " + emitType);
      }
    }

    @Override
    public String toString() {
      try {
        switch (containerType()) {
        case LIST:
        case SET:
          return String.format(
            "<%s type:%s size:%s>",
            emitType.getSimpleName(),
            getContainerSchemaType().getValueType().getProtocolType().getType(),
            size()
          );
        case MAP:
          final MapSchemaType schema = (MapSchemaType) getContainerSchemaType();
          return String.format(
            "<%s keyType:%s valueType:%s size:%s>",
            emitType.getSimpleName(),
            schema.getKeyType().getProtocolType().getType(),
            schema.getValueType().getProtocolType().getType(),
            size()
          );
        default:
          throw new IllegalStateException();
        }
      } catch (TException e) {
        throw new IllegalStateException(e);
      }
    }

    @Override
    public final ContainerType containerType() {
      return containerType;
    }

    public final ContainerSchemaType getContainerSchemaType() {
      return schemaType;
    }

    protected void setList(TList obj) {
//      this.elemType = obj.elemType;
//      this.size = obj.size;
    }

    protected void setSet(TSet obj) {
//      this.elemType = obj.elemType;
//      this.size = obj.size;
    }

    protected void setMap(TMap obj) {
//      this.elemType = obj.elemType;
//      this.size = obj.size;
    }

    public TList emitList() throws TException {
      return new TList(
        getContainerSchemaType().getProtocolType().getType(),
        size()
      );
    }

    public TSet emitSet() throws TException {
      return new TSet(
        getContainerSchemaType().getProtocolType().getType(),
        size()
      );
    }

    public TMap emitMap() throws TException {
      final MapSchemaType schemaType = (MapSchemaType) getContainerSchemaType();
      return new TMap(
        schemaType.getKeyType().getProtocolType().getType(),
        schemaType.getValueType().getProtocolType().getType(),
        size()
      );
    }

    protected abstract int size() throws TException;

  }

}
