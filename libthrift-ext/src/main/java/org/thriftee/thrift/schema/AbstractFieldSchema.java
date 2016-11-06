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

import static org.thriftee.thrift.schema.SchemaBuilderException.ensureNotNull;

import java.util.Collection;

public abstract class AbstractFieldSchema<
    P extends BaseSchema<?, ?>,
    T extends BaseSchema<P, T>
  > extends BaseSchema<P, T> {

  private final Short _identifier;

  private final SchemaType _type;

  private final Requiredness _requiredness;

  protected AbstractFieldSchema(
      final Class<P> parentClass,
      final Class<T> thisClass,
      final P parent,
      final String name,
      final String doc,
      final Collection<ThriftAnnotation> annotations,
      final SchemaType type, 
      final Requiredness required, 
      final Short identifier) throws SchemaBuilderException {
    super(parentClass, thisClass, parent, name, doc, annotations);
    this._type = ensureNotNull("type", type);
    this._requiredness = required == null ? Requiredness.NONE : required;
    this._identifier = ensureNotNull("identifier", identifier); 
  }

  public String getName() {
    return super.getName();
  }

  public Short getIdentifier() {
    return _identifier;
  }

  public SchemaType getType() {
    return _type;
  }

  public Requiredness getRequiredness() {
    return _requiredness;
  }

  @Override
  public String toString() {
    return String.format(
      "%s[name=%s, type=%s, requiredness=%s]",
      getClass().getSimpleName(), 
      getName(), 
      getType().getProtocolType(),
      getRequiredness()
    );
  }

  private static final long serialVersionUID = 4332069454537397041L;

  public static enum Requiredness {
    REQUIRED, OPTIONAL, NONE;
  }

  public static abstract class AbstractFieldBuilder<
    Parent extends BaseSchema<?, Parent>, 
    This extends BaseSchema<Parent, This>, 
    ParentBuilder extends AbstractSchemaBuilder<?, Parent, ?, ?>, 
    Builder extends AbstractFieldBuilder<Parent, This, ParentBuilder, Builder>
  > extends AbstractSchemaBuilder<Parent, This, ParentBuilder, Builder> {

    private Requiredness required = Requiredness.NONE;

    private SchemaType type;

    private Short identifier;

    protected AbstractFieldBuilder(
        final ParentBuilder parentBuilder, final Class<Builder> thisClass) {
      super(parentBuilder, thisClass);
    }

    public final Builder type(SchemaType type) {
      this.type = type;
      return $this;
    }

    public final Builder requiredness(Requiredness required) {
      this.required = required;
      return $this;
    }

    public final Builder identifier(Short _identifier) {
      this.identifier = _identifier;
      return $this;
    }

    public final Builder identifier(Integer _identifier) {
      this.identifier = _identifier.shortValue();
      return $this;
    }

    public final Builder identifier(Long _identifier) {
      this.identifier = _identifier.shortValue();
      return $this;
    }

    protected final SchemaType getType() {
      return this.type;
    }

    protected final Requiredness getRequiredness() {
      return this.required;
    }

    protected final Short getIdentifier() {
      return this.identifier;
    }

    @Override
    protected This build(Parent _parent) throws SchemaBuilderException {
      super._validate();
      if (type == null) {
        throw SchemaBuilderException.typeCannotBeNull(_fieldTypeName());
      }
      This result = _buildInstance(_parent);
      return result;
    }

    protected abstract String _fieldTypeName();

    protected abstract This _buildInstance(Parent _parent)
        throws SchemaBuilderException;

    @Override
    protected String[] toStringFields() {
      return new String[] {
        "name", "annotations", "type", "required", "identifier"
      };
    }

  }

}
