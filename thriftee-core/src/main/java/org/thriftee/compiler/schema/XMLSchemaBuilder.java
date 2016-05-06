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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.apache.thrift.xml.idl.Const;
import org.apache.thrift.xml.idl.Document;
import org.apache.thrift.xml.idl.Field;
import org.apache.thrift.xml.idl.IDL;
import org.apache.thrift.xml.idl.Include;
import org.apache.thrift.xml.idl.Method;
import org.apache.thrift.xml.idl.Service;
import org.apache.thrift.xml.idl.Struct;
import org.apache.thrift.xml.idl.ThriftEnum;
import org.apache.thrift.xml.idl.ThriftEnum.Member;
import org.apache.thrift.xml.idl.ThriftException;
import org.apache.thrift.xml.idl.ThriftType;
import org.apache.thrift.xml.idl.Typedef;
import org.apache.thrift.xml.idl.Union;
import org.thriftee.compiler.schema.AbstractFieldSchema.AbstractFieldBuilder;
import org.thriftee.compiler.schema.AbstractFieldSchema.Requiredness;
import org.thriftee.compiler.schema.AbstractStructSchema.AbstractStructSchemaBuilder;
import org.thriftee.compiler.schema.SchemaReference.Type;
import org.thriftee.compiler.schema.ThriftSchema.Builder;
import org.thriftee.framework.SchemaBuilderConfig;

/**
 * <p>Builds a {@link ThriftSchema} from a JAXB model of the output from the
 * Thrift compiler's XML generator.</p>
 * @author bcg
 */
public final class XMLSchemaBuilder implements SchemaBuilder {

  @Override
  public synchronized ThriftSchema buildSchema(final SchemaBuilderConfig config)
      throws SchemaBuilderException {
    try {
      this.model = JAXB.unmarshal(config.globalXmlFile(), IDL.class);
      this.schemaBuilder = new Builder().name("ThriftEE");
      return translate(model, "ThriftEE");
    } finally {
      this.model = null;
      this.schemaBuilder = null;
    }
  }

  private IDL model;

  private ThriftSchema.Builder schemaBuilder;

  protected ThriftSchema translate(final IDL idl, final String name)
      throws SchemaBuilderException {
    for (final Document document : idl.getDocument()) {
      translate(schemaBuilder, document);
    }
    return schemaBuilder.build();
  }

  protected ModuleSchema.Builder translate(
        final ThriftSchema.Builder parentBuilder, final Document doc
      ) throws SchemaBuilderException {
    final ModuleSchema.Builder val = parentBuilder.addModule(doc.getName());
    final Set<String> includes = new LinkedHashSet<>();
    for (final Object obj : doc.getIncludeOrNamespace()) {
      if (obj instanceof Include) {
        final Include include = (Include) obj;
        includes.add(include.getName());
      }
    }
    val.addIncludes(includes);
    final List<Object> definitions = doc.getExceptionOrTypedefOrService();
    for (final Object definition : definitions) {
      if (definition instanceof Service) {
        translate(val, (Service) definition);
      } else if (definition instanceof Struct) {
        translate(val, (Struct) definition);
      } else if (definition instanceof Union) {
        translate(val, (Union) definition);
      } else if (definition instanceof ThriftEnum) {
        translate(val, (ThriftEnum) definition);
      } else if (definition instanceof ThriftException) {
        translate(val, (ThriftException) definition);
      } else if (definition instanceof Const) {
        continue; // TODO: implement
      } else if (definition instanceof Typedef) {
        translate(val, (Typedef) definition);
      } else {
        throw new SchemaBuilderException(
          SchemaBuilderException.Messages.SCHEMA_102,
          definition.getClass()
        );
      }
    }
    return val;
  }

  protected ExceptionSchema.Builder translate(
        final ModuleSchema.Builder parentBuilder, 
        final ThriftException _exception
      ) throws SchemaBuilderException {
    final ExceptionSchema.Builder val = parentBuilder.addException(
      _exception.getName()
    );
    final List<Field> fields = _exception.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }
  
  UnionSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Union _union) throws SchemaBuilderException {
    final UnionSchema.Builder val = parentBuilder.addUnion(_union.getName());
    final List<Field> fields = _union.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected StructSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Struct _struct) throws SchemaBuilderException {
    final StructSchema.Builder val = parentBuilder.addStruct(_struct.getName());
    final List<Field> fields = _struct.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected EnumSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final ThriftEnum _enum) {
    EnumSchema.Builder val = parentBuilder.addEnum(_enum.getName());
    final List<Member> fields = _enum.getMember();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Member field = fields.get(i);
      translate(val, field);
    }
    return val;
  }

  protected EnumValueSchema.Builder translate(
      final EnumSchema.Builder parentBuilder, final Member field) {
    EnumValueSchema.Builder val = parentBuilder.addEnumValue(field.getName());
    /*
    // TODO: support explicit values for enum fields in schema model
    if (field.getExplicitValue().isPresent()) {
      val.explicitValue(field.getExplicitValue().get());
    }
    */
    return val;
  }

  protected TypedefSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final Typedef _typedef) throws SchemaBuilderException {
    TypedefSchema.Builder val = parentBuilder.addTypedef(_typedef.getName());
    val.type(translate(_typedef));
    return val;
  }

  protected ServiceSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Service _service) throws SchemaBuilderException {
    ServiceSchema.Builder val = parentBuilder.addService(_service.getName());
    if (_service.getParentId() != null || _service.getParentModule() != null) {
      val.parentService(_service.getParentModule()+"."+_service.getParentId());
    }
    final List<Method> methods = _service.getMethod();
    for (int i = 0, c = methods.size(); i < c; i++) {
      Method method = methods.get(i);
      translate(val, method);
    }
    return val;
  }

  protected MethodSchema.Builder translate(
      ServiceSchema.Builder parentBuilder, 
      Method _method) throws SchemaBuilderException {
    final MethodSchema.Builder val = parentBuilder
        .addMethod(_method.getName())
        .oneway(_method.isOneway() == null ? Boolean.FALSE : _method.isOneway())
        .returnType(translate(_method.getReturns()));
    final List<Field> arguments = _method.getArg(); 
    for (int i = 0, c = arguments.size(); i < c; i++) {
      final Field field = arguments.get(i);
      translateArgument(val, field);
    }
    final List<Field> exceptions = _method.getThrows(); 
    for (int i = 0, c = exceptions.size(); i < c; i++) {
      final Field field = exceptions.get(i);
      translateThrows(val, field);
    }
    return val;
  }
  
  protected <B extends AbstractFieldBuilder<?, ?, PB, B>, 
          PB extends AbstractStructSchemaBuilder<?, ?, ?, B, PB>> 
      B translateField(PB parentBuilder, Field _field) throws SchemaBuilderException {
    B field = parentBuilder.addField(_field.getName());
    _translate(field, _field);
    return field;
  }

  protected MethodThrowsSchema.Builder translateThrows(
      MethodSchema.Builder parentBuilder, 
      Field field ) throws SchemaBuilderException {
    MethodThrowsSchema.Builder exc = parentBuilder.addThrows(field.getName());
    _translate(exc, field);
    return exc;
  }

  protected MethodArgumentSchema.Builder translateArgument(
      MethodSchema.Builder parentBuilder, 
      Field field) throws SchemaBuilderException {
    MethodArgumentSchema.Builder arg = parentBuilder.addArgument(field.getName());
    _translate(arg, field);
    return arg;
  }

  protected <T extends AbstractFieldBuilder<?, ?, ?, ?>> T _translate(
        final T arg, 
        final Field field
      ) throws SchemaBuilderException {
    arg.identifier(field.getFieldId());
    if (field.getRequired() != null) {
      switch (field.getRequired()) {
      case REQUIRED:
        arg.requiredness(Requiredness.REQUIRED);
        break;
      case OPTIONAL:
        arg.requiredness(Requiredness.OPTIONAL);
        break;
      default:
        arg.requiredness(null);
        break;
      }
    } else {
      arg.requiredness(null);
    }
    arg.type(translate(field));
    return arg;
  }

  protected ISchemaType translate(ThriftType ttype) throws SchemaBuilderException {
    if (ttype == null) {
      throw new IllegalArgumentException("ThriftType cannot be null");
    }
    switch (ttype.getType()) {
    case ID:
      return schemaBuilder.referenceTo(new SchemaReference(
        resolveSchemaReferenceType(ttype),
        ttype.getTypeModule(), 
        ttype.getTypeId()
      ));
    case MAP:
      return new MapSchemaType(
        translate(ttype.getKeyType()),
        translate(ttype.getValueType())
      );
    case LIST:
      return new ListSchemaType(translate(ttype.getElemType()));
    case SET:
      return new SetSchemaType(translate(ttype.getElemType()));
    case BOOL:
      return PrimitiveTypeSchema.BOOL;
    case I_8:
    case BYTE:
      return PrimitiveTypeSchema.BYTE;
    case I_16:
      return PrimitiveTypeSchema.I16;
    case I_32:
      return PrimitiveTypeSchema.I32;
    case I_64:
      return PrimitiveTypeSchema.I64;
    case DOUBLE:
      return PrimitiveTypeSchema.DOUBLE;
    case STRING:
      return PrimitiveTypeSchema.STRING;
    case BINARY:
      return PrimitiveTypeSchema.BINARY;
    case VOID:
      return PrimitiveTypeSchema.VOID;
    default:
      throw new IllegalStateException("unknown ThriftType: " + ttype.getType());
    }
  }

  private SchemaReference.Type resolveSchemaReferenceType(ThriftType ttype) {
    final Document doc = resolveDocument(ttype.getTypeModule());
    final String typename = ttype.getTypeId();
    for (final Object def : doc.getExceptionOrTypedefOrService()) {
      final String defname;
      final Type type;
      if (def instanceof Struct) {
        defname = ((Struct)def).getName();
        type = Type.STRUCT;
      } else if (def instanceof Union) {
        defname = ((Union)def).getName();
        type = Type.UNION;
      } else if (def instanceof ThriftEnum) {
        defname = ((ThriftEnum)def).getName();
        type = Type.ENUM;
      } else if (def instanceof ThriftException) {
        defname = ((ThriftException)def).getName();
        type = Type.EXCEPTION;
      } else if (def instanceof Typedef) {
        defname = ((Typedef)def).getName();
        type = Type.TYPEDEF;
      } else {
        continue;
      }
      if (typename.equals(defname)) {
        return type;
      }
    }
    throw new IllegalArgumentException(
      "could not find ttype: " + ttype.getTypeModule() + "." + ttype.getTypeId()
    );
  }
//throw new SchemaBuilderException(
//SchemaBuilderException.Messages.SCHEMA_102,
//definition.getClass()
//);

  private Document resolveDocument(String name) {
    for (final Document doc : model.getDocument()) {
      if (name.equals(doc.getName())) {
        return doc;
      }
    }
    throw new IllegalArgumentException("could not find document: " + name);
  }

  interface SchemaContextCreatedListener {
    void schemaContextCreated(SchemaContext ctx);
  }

}
