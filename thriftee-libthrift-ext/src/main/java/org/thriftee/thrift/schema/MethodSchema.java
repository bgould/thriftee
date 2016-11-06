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
package org.thriftee.thrift.schema;

import java.util.Collection;
import java.util.Map;

public final class MethodSchema extends BaseSchema<ServiceSchema, MethodSchema> {

  private static final long serialVersionUID = -6863308018762813185L;

  private final boolean oneway;

  private final MethodArgsSchema arguments;

  private final MethodResultSchema result;

  protected MethodSchema(
      final ServiceSchema parent,
      final String name,
      final String doc,
      final Collection<ThriftAnnotation> annotations,
      final boolean oneway,
      final MethodArgsSchema.Builder arguments,
      final MethodResultSchema.Builder result) throws SchemaBuilderException {
    super(
      ServiceSchema.class, MethodSchema.class, parent, name, doc, annotations);
    this.oneway = oneway;
    this.arguments = arguments.build(this);
    this.result = result.build(this);
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Map<String, ThriftAnnotation> getAnnotations() {
    return super.getAnnotations();
  }

  public boolean isOneway() {
    return oneway;
  }

  public SchemaType getReturnType() {
    return this.result.getReturnType();
  }

  public Map<String, MethodArgSchema> getArguments() {
    return this.arguments.getFields();
  }

  public Map<String, MethodThrowsSchema> getExceptions() {
    return this.result.getExceptions();
  }

  public MethodArgsSchema getArgumentStruct() {
    return arguments;
  }

  public MethodResultSchema getResultStruct() {
    return result;
  }

  public static final class Builder extends AbstractSchemaBuilder<
      ServiceSchema, 
      MethodSchema, 
      ServiceSchema.Builder, 
      Builder> {

    protected Builder(ServiceSchema.Builder parentBuilder) {
      super(parentBuilder, Builder.class);
      this._arguments = new MethodArgsSchema.Builder(this);
      this._result = new MethodResultSchema.Builder(this);
    }

    private boolean _oneway;

    private MethodArgsSchema.Builder _arguments;

    private MethodResultSchema.Builder _result;

    @Override
    public Builder name(final String name) {
      this._arguments.name(name + "_args");
      this._result.name(name + "_result");
      return super.name(name);
    }

    public Builder oneway(final boolean oneway) {
      this._oneway = oneway;
      return this;
    }

    public Builder returnType(final SchemaType type) {
      this._result.returnType(type);
      return this;
    }

    public Builder defaultValue(final SchemaValue value) {
      return this;
    }

    public MethodArgSchema.Builder addArgument(final String name) {
      return this._arguments.addArgument(name);
    }

    public MethodThrowsSchema.Builder addThrows(final String name) {
      return this._result.addThrows(name);
    }

    @Override
    protected MethodSchema build(final ServiceSchema parent) 
        throws SchemaBuilderException {
      super._validate();
      MethodSchema result = new MethodSchema(
        parent,
        getName(),
        getDoc(),
        getAnnotations(),
        this._oneway, 
        this._arguments, 
        this._result
      );
      return result;
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
