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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.thriftee.thrift.schema.SchemaContext.SchemaContextCreatedListener;

public final class ServiceSchema
    extends BaseSchema<ModuleSchema, ServiceSchema>
    implements SchemaContextCreatedListener {

  private static final long serialVersionUID = 419978455931497309L;

  private final Map<String, MethodSchema> _declaredMethods;

  private Map<String, MethodSchema> _allMethods;

  private final String _parentService;

  private final String _xmlTargetNamespace;

  private ServiceSchema(
      final ModuleSchema module,
      final String name,
      final String doc,
      final Collection<ThriftAnnotation> annots,
      final String _xmlTargetNamespace,
      final String parentService,
      final Collection<MethodSchema.Builder> methods
    ) throws SchemaBuilderException {
    super(ModuleSchema.class, ServiceSchema.class, module, name, doc, annots);
    if (_xmlTargetNamespace == null) {
      this._xmlTargetNamespace = getModule().getXmlTargetNamespace()+"/"+name;
    } else {
      this._xmlTargetNamespace = _xmlTargetNamespace;
    }
    this._parentService = parentService;
    this._declaredMethods = toMap(this, methods);
  }

  @Override
  public void schemaContextCreated(final SchemaContext ctx)
      throws SchemaBuilderException {
    final Map<String, MethodSchema> methods = new HashMap<>();
    for (ServiceSchema s = this; s != null; s = s.getParentServiceSchema()) {
      for (final MethodSchema m : s.getDeclaredMethods().values()) {
        if (methods.containsKey(m.getName())) {
          throw new SchemaBuilderException(
            "Duplicate method definition found for " +
            new MethodIdentifier(getModule().getName(), getName(), m.getName())
          );
        }
        methods.put(m.getName(), m);
      }
    }
    _allMethods = Collections.unmodifiableMap(methods);
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Map<String, ThriftAnnotation> getAnnotations() {
    return super.getAnnotations();
  }

  public String getXmlTargetNamespace() {
    return this._xmlTargetNamespace;
  }

  public String getParentService() {
    return this._parentService;
  }

  public Map<String, MethodSchema> getDeclaredMethods() {
    return _declaredMethods;
  }

  public Map<String, MethodSchema> getMethods() {
    return _allMethods;
  }

  public MethodSchema findMethod(String name) throws SchemaException {
    final MethodSchema result = _allMethods.get(name);
    if (result == null) {
      throw SchemaException.methodNotFound(
        new MethodIdentifier(getModule().getName(), getName(), name)
      );
    } else {
      return result;
    }
  }

  public ServiceSchema getParentServiceSchema() {
    if (_parentService == null) {
      return null;
    } else {
      return getSchemaContext().resolveService(_parentService);
    }
  }

  public static class Builder extends AbstractSchemaBuilder<
    ModuleSchema, ServiceSchema, ModuleSchema.Builder, ServiceSchema.Builder> {

    private String parentService;

    private List<MethodSchema.Builder> methods = new LinkedList<>();

    private String xmlTargetNamespace;

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

    public Builder xmlTargetNamespace(String namespace) {
      this.xmlTargetNamespace = namespace;
      return this;
    }

    @Override
    protected ServiceSchema build(final ModuleSchema _parent)
        throws SchemaBuilderException {
      super._validate();
      final ServiceSchema result = new ServiceSchema(
        _parent,
        getName(),
        getDoc(),
        getAnnotations(),
        this.xmlTargetNamespace,
        this.parentService,
        methods
      );
      getParentBuilder().getParentBuilder().addListener(result);
      return result;
    }

    @Override
    protected String[] toStringFields() {
      return new String[] { "name", "annotations", "parentService", "methods" };
    }

  }
}
