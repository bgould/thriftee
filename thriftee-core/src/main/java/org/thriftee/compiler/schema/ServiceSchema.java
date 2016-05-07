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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.thriftee.compiler.schema.ServiceSchema.Builder;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class ServiceSchema extends BaseSchema<ModuleSchema, ServiceSchema> {

  public static final int THRIFT_INDEX_NAME = 1;

  public static final int THRIFT_INDEX_ANNOTATIONS = THRIFT_INDEX_NAME + 1;

  public static final int THRIFT_INDEX_PARENT_SERVICE = THRIFT_INDEX_ANNOTATIONS + 1;

  public static final int THRIFT_INDEX_METHODS = THRIFT_INDEX_PARENT_SERVICE + 1;

  private static final long serialVersionUID = 419978455931497309L;

  private final Map<String, MethodSchema> methods;

  private final String parentService;

  private ServiceSchema(
      ModuleSchema module, 
      String _name,
      Collection<ThriftAnnotation> _annotations,
      String parentService, 
      Collection<MethodSchema.Builder> _methods
    ) throws SchemaBuilderException {
    super(ModuleSchema.class, ServiceSchema.class, module, _name, _annotations);
    this.parentService = parentService;
    this.methods = toMap(this, _methods);
  }

  public ModuleSchema getModule() {
    return getParent();
  }

  @ThriftField(THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }

  @ThriftField(THRIFT_INDEX_ANNOTATIONS)
  public Map<String, ThriftAnnotation> getAnnotations() {
    return super.getAnnotations();
  }

  @ThriftField(THRIFT_INDEX_PARENT_SERVICE)
  public String getParentService() {
    return this.parentService;
  }

  @ThriftField(THRIFT_INDEX_METHODS)
  public Map<String, MethodSchema> getMethods() {
    return methods;
  }

  public MethodSchema findMethod(String name) throws TException {
    for (ServiceSchema svc = this; svc != null; svc = svc.getParentServiceSchema()) {
      final MethodSchema method = svc.getMethods().get(name);
      if (method != null) {
        return method;
      }
    }
    throw new TException(String.format(
      "method '%s' not found on %s", name, getModule()+"."+getName()
    ));
  }

  public ServiceSchema getParentServiceSchema() {
    if (parentService == null) {
      return null;
    } else {
      return getSchemaContext().resolveService(parentService);
    }
  }

  public static class Builder extends AbstractSchemaBuilder<
    ModuleSchema, ServiceSchema, ModuleSchema.Builder, ServiceSchema.Builder> {

    private String parentService;

    private List<MethodSchema.Builder> methods = new LinkedList<>();

    public Builder() 
        throws NoArgConstructorOnlyExistsForSwiftValidationException {
      this(null);
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

    Builder(final ModuleSchema.Builder parentBuilder) {
      super(parentBuilder, ServiceSchema.Builder.class);
    }

    public MethodSchema.Builder addMethod(final String _name) {
      MethodSchema.Builder result = new MethodSchema.Builder(this);
      methods.add(result);
      return result.name(_name);
    }

    public Builder parentService(String parentService) {
      this.parentService = parentService;
      return this;
    }

    @Override
    protected ServiceSchema _build(final ModuleSchema _parent) 
        throws SchemaBuilderException {
      super._validate();
      final ServiceSchema result = new ServiceSchema(
        _parent, 
        getName(), 
        getAnnotations(), 
        this.parentService, 
        methods
      );
      return result;
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "annotations", "parentService", "methods" };
    }

    @Override
    @ThriftConstructor
    public ServiceSchema build() throws SchemaBuilderException {
      throw new NoArgConstructorOnlyExistsForSwiftValidationException();
    }

  }
}
