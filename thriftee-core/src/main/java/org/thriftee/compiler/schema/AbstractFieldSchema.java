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
package org.thriftee.compiler.schema;

import java.util.Collection;

import org.thriftee.compiler.schema.SchemaBuilderException.Messages;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftField;

public abstract class AbstractFieldSchema<P extends BaseSchema<?, ?>, T extends BaseSchema<P, T>> extends BaseSchema<P, T> {

  public static final int THRIFT_INDEX_NAME = 1;
  
  public static final int THRIFT_INDEX_IDENTIFIER = THRIFT_INDEX_NAME + 1;
  
  public static final int THRIFT_INDEX_TYPE = THRIFT_INDEX_IDENTIFIER + 1;
  
  public static final int THRIFT_INDEX_REQUIRED = THRIFT_INDEX_TYPE + 1;
  
  public static final int THRIFT_INDEX_ANNOTATIONS = THRIFT_INDEX_REQUIRED + 1;
  
  private final Short identifier;

  private final ISchemaType type;

  private final Requiredness requiredness;

  //private final ConstantValue defaultValue;

  protected AbstractFieldSchema(
      Class<P> parentClass,
      Class<T> thisClass,
      P _parent, 
      String _name, 
      Collection<ThriftAnnotation> _annotations,
      ISchemaType _type, 
      Requiredness _requiredness, 
      Short _identifier) throws SchemaBuilderException {
    super(parentClass, thisClass, _parent, _name, _annotations);
    this.type = _type;
    this.requiredness = _requiredness;
    this.identifier = _identifier; 
  }
  
  /*verifyAndConvertToInteger(_identifier);
  }
  
  private final Integer verifyAndConvertToInteger(Long _identifier) {
    if (_identifier == null) {
      return null;
    }
    if (_identifier.longValue() > Integer.MAX_VALUE || _identifier.longValue() < Integer.MIN_VALUE) {
      throw new IllegalArgumentException(
        "Loss of precision would have occurred on conversion from long to int.");
    } else {
      return _identifier.intValue();
    }
  }*/
  
  @ThriftField(THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }

  @ThriftField(THRIFT_INDEX_IDENTIFIER)
  public Short getIdentifier() {
    return identifier;
  }

  @ThriftField(THRIFT_INDEX_TYPE)
  public ThriftSchemaType getType() {
    return ThriftSchemaType.wrap(type);
  }

  @ThriftField(THRIFT_INDEX_REQUIRED)
  public Requiredness getRequiredness() {
    return requiredness;
  }

  private static final long serialVersionUID = 4332069454537397041L;
  
  @ThriftEnum
  public static enum Requiredness {
    REQUIRED, OPTIONAL, NONE;
  }

  public static abstract class AbstractFieldBuilder<
    P extends BaseSchema<?, P>, 
    T extends BaseSchema<P, T>, 
    PB extends AbstractSchemaBuilder<?, P, ?, ?>, 
    B extends AbstractFieldBuilder<P, T, PB, B>
  > extends AbstractSchemaBuilder<P, T, PB, B> {

    private Requiredness required;
    
    private ISchemaType type;
    
    private Short identifier;
    
    protected AbstractFieldBuilder(PB parentBuilder, Class<B> thisClass) {
      super(parentBuilder, thisClass);
    }

    public final B type(ISchemaType type) {
      this.type = type;
      return $this;
    }

    public final B requiredness(Requiredness required) {
      this.required = required;
      return $this;
    }
    
    public final B identifier(Short _identifier) {
      this.identifier = _identifier;
      return $this;
    }
    
    public final B identifier(Integer _identifier) {
      this.identifier = _identifier.shortValue();
      return $this;
    }
    
    public final B identifier(Long _identifier) {
      this.identifier = _identifier.shortValue();
      return $this;
    }
    
    protected final ISchemaType getType() {
      return this.type;
    }
    
    protected final Requiredness getRequiredness() {
      return this.required;
    }
    
    protected final Short getIdentifier() {
      return this.identifier;
    }
    
    @Override
    protected T _build(P _parent) throws SchemaBuilderException {
      super._validate();
      if (type == null) {
        throw new SchemaBuilderException(Messages.SCHEMA_002, _fieldTypeName());
      }
      T result = _buildInstance(_parent);
      return result;
    }
    
    protected abstract String _fieldTypeName();
    
    protected abstract T _buildInstance(P _parent) throws SchemaBuilderException;

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "annotations", "type", "required", "identifier" };
    }

  }
  
}
