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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.thriftee.thrift.schema.SchemaContext.SchemaContextCreatedListener;

public final class ThriftSchema extends BaseSchema<ThriftSchema, ThriftSchema> {

  public static final int THRIFT_INDEX_NAME = 1;

  public static final int THRIFT_INDEX_MODULES = THRIFT_INDEX_NAME + 1;

  private static final long serialVersionUID = -8572014932719192064L;

  private final Map<String, ModuleSchema> modules;

  private final SchemaContext schemaContext;

  private ThriftSchema(
        final String name,
        final Collection<ModuleSchema.Builder> modules,
        final Collection<SchemaContextCreatedListener> listeners
      ) throws SchemaBuilderException {
    super(ThriftSchema.class, ThriftSchema.class, null, name, null, null);
    this.schemaContext = new SchemaContext(this);
    this.modules = toMap(this, modules);
    for (final SchemaContextCreatedListener lstnr : listeners) {
      lstnr.schemaContextCreated(schemaContext);
    }
  }

  @Override
  public String getName() {
    return super.getName();
  }

  public Map<String, ModuleSchema> getModules() {
    return this.modules;
  }

  public ModuleSchema findModule(String moduleName) {
    return getSchemaContext().resolveModule(moduleName);
  }

  public ServiceSchema findService(String moduleName, String serviceName) {
    return getSchemaContext().resolveService(moduleName, serviceName);
  }

  public SchemaType findType(String moduleName, String typeName) {
    return getSchemaContext().resolveType(moduleName, typeName);
  }

  public MethodSchema findMethod(MethodIdentifier id) {
    final ModuleSchema module = getModules().get(id.getModuleName());
    if (module == null) {
      throw new IllegalArgumentException(
        String.format("module '%s' not found", id.getModuleName())
      );
    }
    final ServiceSchema svc = module.getServices().get(id.getServiceName());
    if (svc == null) {
      throw new IllegalArgumentException(String.format(
        "service '%s' not found in module '%s'",
        id.getServiceName(), id.getModuleName()
      ));
    }
    final MethodSchema mthd = svc.getDeclaredMethods().get(id.getMethodName());
    if (mthd == null) {
      throw new IllegalArgumentException(String.format(
        "service '%s.%s' does not have a method name '%s'",
        id.getModuleName(), id.getServiceName(), id.getMethodName()
      ));
    }
    return mthd;
  }

  public AbstractStructSchema<?, ?, ?, ?> structSchemaFor(SchemaType typeRef) {
    if (typeRef instanceof AbstractStructSchema<?, ?, ?, ?>) {
      return (AbstractStructSchema<?, ?, ?, ?>) typeRef;
    }
    return getSchemaContext().resolveStructSchema(typeRef);
  }

  public ExceptionSchema applicationExceptionSchema() {
    return findType("org.apache.thrift", "TApplicationException").
        castTo(ExceptionSchema.class);
  }

  @Override
  SchemaContext getSchemaContext() {
    return this.schemaContext;
  }

  public static final class Builder extends AbstractSchemaBuilder<
      ThriftSchema,
      ThriftSchema,
      ThriftSchema.Builder,
      ThriftSchema.Builder> {

    public Builder() {
      super(null, ThriftSchema.Builder.class);
      addModule("org.apache.thrift").addException("TApplicationException").
        addField("type").type(PrimitiveTypeSchema.I32).identifier(2).end().
        addField("message").type(PrimitiveTypeSchema.STRING).identifier(1);
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
      addListener(result);
      return result;
    }

    public MapSchemaType map(SchemaType keyType, SchemaType valueType)
        throws SchemaBuilderException {
      return new MapSchemaType(keyType, valueType);
    }

    public SetSchemaType set(SchemaType valueType)
        throws SchemaBuilderException {
      return new SetSchemaType(valueType);
    }

    public ListSchemaType list(SchemaType valueType)
        throws SchemaBuilderException {
      return new ListSchemaType(valueType);
    }

    @Override
    protected ThriftSchema build(ThriftSchema parent)
        throws SchemaBuilderException {
      super._validate();
      return new ThriftSchema(getName(), this.modules, lstnrs);
    }

    public ThriftSchema build() throws SchemaBuilderException {
      return build(null);
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "modules" };
    }

    void addListener(SchemaContextCreatedListener lstnr) {
      lstnrs.add(lstnr);
    }
  }

}
