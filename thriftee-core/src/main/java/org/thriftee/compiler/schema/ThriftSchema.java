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

import org.thriftee.compiler.schema.ThriftSchema.Builder;
import org.thriftee.compiler.schema.XMLSchemaBuilder.SchemaContextCreatedListener;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct(builder=Builder.class)
public final class ThriftSchema extends BaseSchema<ThriftSchema, ThriftSchema> {

  public static final int THRIFT_INDEX_NAME = 1;

  public static final int THRIFT_INDEX_MODULES = THRIFT_INDEX_NAME + 1;

  private static final long serialVersionUID = -8572014932719192064L;

  private final Map<String, ModuleSchema> modules;

  private final SchemaContext schemaContext;

  public ThriftSchema(
        final String name, 
        final Collection<ModuleSchema.Builder> modules,
        final Collection<SchemaContextCreatedListener> listeners
      ) throws SchemaBuilderException {
    super(ThriftSchema.class, ThriftSchema.class, null, name, null);
    this.schemaContext = new SchemaContext(this);
    this.modules = toMap(this, modules);
    for (final SchemaContextCreatedListener lstnr : listeners) {
      lstnr.schemaContextCreated(schemaContext);
    }
  }

  @ThriftField(THRIFT_INDEX_NAME)
  public String getName() {
    return super.getName();
  }

  @ThriftField(THRIFT_INDEX_MODULES)
  public Map<String, ModuleSchema> getModules() {
    return this.modules;
  }

  public ModuleSchema findModule(String moduleName) {
    return getSchemaContext().resolveModule(moduleName);
  }

  public ServiceSchema findService(String moduleName, String serviceName) {
    return getSchemaContext().resolveService(moduleName, serviceName);
  }

  public ISchemaType findType(String moduleName, String typeName) {
    return getSchemaContext().resolveType(moduleName, typeName);
  }

  public MethodSchema findMethod(MethodIdentifier id) {
    final ModuleSchema module = getModules().get(id.getModuleName());
    if (module == null) {
    throw new IllegalArgumentException(
      String.format("module '%s' not found", id.getModuleName())
    );
    }
    final ServiceSchema service = module.getServices().get(id.getServiceName());
    if (service == null) {
    throw new IllegalArgumentException(String.format(
      "service '%s' not found in module '%s'",
      id.getServiceName(), id.getModuleName()
    ));
    }
    final MethodSchema method = service.getMethods().get(id.getMethodName());
    if (method == null) {
    throw new IllegalArgumentException(String.format(
      "service '%s.%s' does not have a method name '%s'",
      id.getModuleName(), id.getServiceName(), id.getMethodName()
    ));
    }
    return method;
  }

  public AbstractStructSchema<?, ?, ?, ?> structSchemaFor(ISchemaType typeRef) {
    if (typeRef instanceof AbstractStructSchema<?, ?, ?, ?>) {
      return (AbstractStructSchema<?, ?, ?, ?>) typeRef;
    }
    return getSchemaContext().resolveStructSchema(typeRef);
  }

//  public ISchemaType resolveType(ISchemaType type) {
//    if (type instanceof ThriftSchemaType) {
//      type = ((ThriftSchemaType) type).unwrap();
//    }
//    if (type instanceof ReferenceSchemaType) {
//      return getSchemaContext().resolveType(type);
//    } else {
//      return type;
//    }
//  }

  @Override
  SchemaContext getSchemaContext() {
    return this.schemaContext;
  }

  public static final class Builder extends AbstractSchemaBuilder<ThriftSchema, ThriftSchema, ThriftSchema.Builder, ThriftSchema.Builder> {

    public Builder() {
      super(null, ThriftSchema.Builder.class);
    }

    private List<ModuleSchema.Builder> modules = new LinkedList<>();

    private List<SchemaContextCreatedListener> lstnrs = new LinkedList<>();

    public ModuleSchema.Builder addModule(String _name) {
      ModuleSchema.Builder result = new ModuleSchema.Builder(this);
      this.modules.add(result);
      return result.name(_name);
    }

    public ReferenceSchemaType referenceTo(SchemaReference reference) {
      final ReferenceSchemaType result = new ReferenceSchemaType(reference);
      lstnrs.add(result);
      return result;
    }

    @Override
    protected ThriftSchema _build(ThriftSchema parent) throws SchemaBuilderException {
      super._validate();
      return new ThriftSchema(getName(), this.modules, lstnrs);
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "modules" };
    }

    @Override
    @ThriftConstructor
    public ThriftSchema build() throws SchemaBuilderException {
      return this._build(null);
    }

  }

}
