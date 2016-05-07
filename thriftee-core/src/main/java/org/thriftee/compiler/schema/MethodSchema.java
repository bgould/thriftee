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
import java.util.Map;

import org.thriftee.compiler.schema.MethodSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class MethodSchema extends BaseSchema<ServiceSchema, MethodSchema> {
  
  public static final int THRIFT_INDEX_NAME = 1;
  
  public static final int THRIFT_INDEX_ONEWAY = THRIFT_INDEX_NAME + 1;
  
  public static final int THRIFT_INDEX_RETURN_TYPE = THRIFT_INDEX_ONEWAY + 1;
  
  public static final int THRIFT_INDEX_ARGUMENTS = THRIFT_INDEX_RETURN_TYPE + 1;
  
  public static final int THRIFT_INDEX_EXCEPTIONS = THRIFT_INDEX_ARGUMENTS + 1;
  
  public static final int THRIFT_INDEX_ANNOTATIONS = THRIFT_INDEX_EXCEPTIONS + 1;
  
  private static final long serialVersionUID = -6863308018762813185L;

  private final boolean oneway;

  private final MethodArgumentSchema arguments;

  private final MethodResultSchema result;

  protected MethodSchema(
      ServiceSchema parent, 
      String name, 
      Collection<ThriftAnnotation> annotations,
      boolean oneway,
      MethodArgumentSchema.Builder arguments,
      MethodResultSchema.Builder result) throws SchemaBuilderException {
    super(ServiceSchema.class, MethodSchema.class, parent, name, annotations);
    this.oneway = oneway;
    this.arguments = arguments._build(this);
    this.result = result._build(this);
  }
  
  @Override
  @ThriftField(THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }

  @Override
  @ThriftField(THRIFT_INDEX_ANNOTATIONS)
  public Map<String, ThriftAnnotation> getAnnotations() {
    return super.getAnnotations();
  }

  @ThriftField(value=THRIFT_INDEX_ONEWAY, name="onewayMethod")
  public boolean isOneway() {
    return oneway;
  }

  @ThriftField(THRIFT_INDEX_RETURN_TYPE)
  public ThriftSchemaType getReturnType() {
    return ThriftSchemaType.wrap(this.result.getReturnType());
  }

  @ThriftField(THRIFT_INDEX_ARGUMENTS)
  public Map<String, MethodArgumentFieldSchema> getArgumentMap() {
    return this.arguments.getFields();
  }

  @ThriftField(THRIFT_INDEX_EXCEPTIONS)
  public Map<String, MethodThrowsSchema> getExceptions() {
    return this.result.getExceptions();
  }

  public MethodArgumentSchema getArgumentStruct() {
    return arguments;
  }

  public MethodResultSchema getResultStruct() {
    return result;
  }

  public static class Builder extends AbstractSchemaBuilder<
      ServiceSchema, 
      MethodSchema, 
      ServiceSchema.Builder, 
      Builder> {

    public Builder() throws NoArgConstructorOnlyExistsForSwiftValidationException {
      this(null);
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

    Builder(ServiceSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
      this._arguments = new MethodArgumentSchema.Builder(this);
      this._result = new MethodResultSchema.Builder(this);
    }

    private boolean _oneway;

    private MethodArgumentSchema.Builder _arguments;

    private MethodResultSchema.Builder _result;

    @Override
    public Builder name(String name) {
      this._arguments.name(name + "_args");
      this._result.name(name + "_result");
      return super.name(name);
    }

    public Builder oneway(boolean oneway) {
      this._oneway = oneway;
      return this;
    }

    public Builder returnType(ISchemaType type) {
      this._result.returnType(type);
      return this;
    }

    public Builder defaultValue(ISchemaValue value) {
      return this;
    }

    public MethodArgumentFieldSchema.Builder addArgument(String name) {
      return this._arguments.addArgument(name);
    }

    public MethodThrowsSchema.Builder addThrows(String name) {
      return this._result.addThrows(name);
    }

    @Override
    protected MethodSchema _build(ServiceSchema parent) throws SchemaBuilderException {
      super._validate();
      MethodSchema result = new MethodSchema(
        parent, 
        getName(),
        getAnnotations(),
        this._oneway, 
        this._arguments, 
        this._result
      );
      return result;
    }

    @Override
    @ThriftConstructor
    public MethodSchema build() throws SchemaBuilderException {
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

    @Override
    protected String[] toStringFields() {
      return new String[] {
        "name",
        "oneway",
        "returnType",
        "arguments",
        "exceptions",
        "annotations"
      };
    }

  }

}
