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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class ModuleSchema extends BaseSchema<ThriftSchema, ModuleSchema> {

  private static final long serialVersionUID = 1973580748761800425L;

  private final Map<String, ExceptionSchema> _exceptions;

  private final Map<String, TypedefSchema> _typedefs;

  private final Map<String, ServiceSchema> _services;

  private final Map<String, StructSchema> _structs;

  private final Map<String, UnionSchema> _unions;

  private final Map<String, EnumSchema> _enums;

  private final Set<String> _includes;

  private ModuleSchema(
      final ThriftSchema parent,
      final String name,
      final String doc,
      final Collection<String> includes,
      final Collection<ExceptionSchema.Builder> exceptions,
      final Collection<TypedefSchema.Builder> typedefs, 
      final Collection<ServiceSchema.Builder> services, 
      final Collection<StructSchema.Builder> structs,
      final Collection<UnionSchema.Builder> unions,
      final Collection<EnumSchema.Builder> enums
    ) throws SchemaBuilderException {
    super(ThriftSchema.class, ModuleSchema.class, parent, name, doc, null);
    this._includes = Collections.unmodifiableSortedSet(new TreeSet<>(includes));
    this._exceptions = toMap(this, exceptions);
    this._typedefs = toMap(this, typedefs);
    this._services = toMap(this, services);
    this._structs = toMap(this, structs);
    this._unions = toMap(this, unions);
    this._enums = toMap(this, enums);
  }

  public String getName() {
    return super.getName();
  }

  public Set<String> getIncludes() {
    return _includes;
  }

  public Map<String, ExceptionSchema> getExceptions() {
    return _exceptions;
  }

  public Map<String, TypedefSchema> getTypedefs() {
    return _typedefs;
  }

  public Map<String, ServiceSchema> getServices() {
    return _services;
  }

  public Map<String, StructSchema> getStructs() {
    return _structs;
  }

  public Map<String, UnionSchema> getUnions() {
    return _unions;
  }

  public Map<String, EnumSchema> getEnums() {
    return _enums;
  }

  public static final class Builder extends AbstractSchemaBuilder<
      ThriftSchema, 
      ModuleSchema, 
      ThriftSchema.Builder, 
      ModuleSchema.Builder
    > {

    private final Set<String> includes = new TreeSet<>();

    private final List<ExceptionSchema.Builder> exceptions = new LinkedList<>();

    private final List<TypedefSchema.Builder> typedefs = new LinkedList<>();

    private final List<ServiceSchema.Builder> services = new LinkedList<>();

    private final List<StructSchema.Builder> structs = new LinkedList<>();

    private final List<UnionSchema.Builder> unions = new LinkedList<>();

    private final List<EnumSchema.Builder> enums = new LinkedList<>();

    Builder(ThriftSchema.Builder parentBuilder) {
      super(parentBuilder, ModuleSchema.Builder.class);
    }

    public Builder addInclude(String include) {
      this.includes.add(include);
      return this;
    }

    public Builder addIncludes(Collection<String> includes) {
      this.includes.addAll(includes);
      return this;
    }

    public ExceptionSchema.Builder addException(final String name) {
      ExceptionSchema.Builder result = new ExceptionSchema.Builder(this);
      this.exceptions.add(result);
      return result.name(name);
    }

    public TypedefSchema.Builder addTypedef(final String name) {
      TypedefSchema.Builder result = new TypedefSchema.Builder(this);
      this.typedefs.add(result);
      return result.name(name);
    }

    public ServiceSchema.Builder addService(final String name) {
      ServiceSchema.Builder result = new ServiceSchema.Builder(this);
      this.services.add(result);
      return result.name(name);
    }

    public StructSchema.Builder addStruct(final String name) {
      StructSchema.Builder result = new StructSchema.Builder(this);
      this.structs.add(result);
      return result.name(name);
    }

    public UnionSchema.Builder addUnion(final String name) {
      UnionSchema.Builder result = new UnionSchema.Builder(this);
      this.unions.add(result);
      return result.name(name);
    }

    public EnumSchema.Builder addEnum(final String name) {
      EnumSchema.Builder result = new EnumSchema.Builder(this);
      this.enums.add(result);
      return result.name(name);
    }

    @Override
    protected ModuleSchema build(ThriftSchema parent)
        throws SchemaBuilderException {
      super._validate();
      final ModuleSchema result = new ModuleSchema(
        parent,
        getName(),
        getDoc(),
        includes,
        exceptions,
        typedefs,
        services,
        structs,
        unions,
        enums
      );
      return result;
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { 
        "name",
        "includes",
        "annotations", 
        "typedefs",
        "services", 
        "structs", 
        "unions", 
        "enums"
      };
    }

  }

}
