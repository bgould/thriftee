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
import org.apache.thrift.xml.idl.Union;
import org.thriftee.compiler.schema.AbstractFieldSchema.AbstractFieldBuilder;
import org.thriftee.compiler.schema.AbstractFieldSchema.Requiredness;
import org.thriftee.compiler.schema.AbstractStructSchema.AbstractStructSchemaBuilder;
import org.thriftee.compiler.schema.ThriftSchema.Builder;
import org.thriftee.framework.SchemaBuilderConfig;

public class XMLSchemaBuilder implements SchemaBuilder {

  @Override
  public ThriftSchema buildSchema(final SchemaBuilderConfig config) 
      throws SchemaBuilderException {
    final IDL model = JAXB.unmarshal(config.globalXmlFile(), IDL.class);
    return translate(model, "ThriftEE");
  }

  public static ThriftSchema translate(final IDL idl, final String name) 
      throws SchemaBuilderException {
    final Builder builder = new Builder().name("ThriftEE");
    for (final Document document : idl.getDocument()) {
      translate(builder, document);
    }
    return builder.build();
  }

  public static ModuleSchema.Builder translate(
      final ThriftSchema.Builder parentBuilder, 
      final Document document
    ) throws SchemaBuilderException {
    final ModuleSchema.Builder val = parentBuilder.addModule(document.getName());
    final Set<String> includes = new LinkedHashSet<>();
    for (final Object obj : document.getIncludeOrNamespace()) {
      if (obj instanceof Include) {
        final Include include = (Include) obj;
        includes.add(include.getName());
      }
    }
    val.addIncludes(includes);
    final List<Object> definitions = document.getExceptionOrTypedefOrService();
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
      } else {
        throw new SchemaBuilderException(
          SchemaBuilderException.Messages.SCHEMA_102,
          definition.getClass()
        );
      }
    }
    return val;
  }
  
  public static ExceptionSchema.Builder translate(
        final ModuleSchema.Builder parentBuilder, 
        final ThriftException _exception
      ) {
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
  
  public static UnionSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Union _union) {
    final UnionSchema.Builder val = parentBuilder.addUnion(_union.getName());
    final List<Field> fields = _union.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }
  
  public static StructSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Struct _struct) {
    final StructSchema.Builder val = parentBuilder.addStruct(_struct.getName());
    final List<Field> fields = _struct.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }
  
  public static EnumSchema.Builder translate(
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
  
  public static EnumValueSchema.Builder translate(
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
  
  public static ServiceSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Service _service) {
    ServiceSchema.Builder val = parentBuilder.addService(_service.getName());
    if (_service.getParentId() != null || _service.getParentModule() != null) {
      throw new UnsupportedOperationException();
      //val.parentService(_service.getParent().get());
    }
    final List<Method> methods = _service.getMethod();
    for (int i = 0, c = methods.size(); i < c; i++) {
      Method method = methods.get(i);
      translate(val, method);
    }
    return val;
  }
  
  public static MethodSchema.Builder translate(
      ServiceSchema.Builder parentBuilder, 
      Method _method) {
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
  
  public static <B extends AbstractFieldBuilder<?, ?, PB, B>, 
          PB extends AbstractStructSchemaBuilder<?, ?, ?, B, PB>> 
      B translateField(PB parentBuilder, Field _field) {
    B field = parentBuilder.addField(_field.getName());
    _translate(field, _field);
    return field;
  }

  public static MethodThrowsSchema.Builder translateThrows(
      MethodSchema.Builder parentBuilder, Field field) {
    MethodThrowsSchema.Builder exc = parentBuilder.addThrows(field.getName());
    _translate(exc, field);
    return exc;
  }

  public static MethodArgumentSchema.Builder translateArgument(
      MethodSchema.Builder parentBuilder, Field field) {
    MethodArgumentSchema.Builder arg = parentBuilder.addArgument(field.getName());
    _translate(arg, field);
    return arg;
  }

  private static <T extends AbstractFieldBuilder<?, ?, ?, ?>> T _translate(
        final T arg, 
        final Field field
      ) {
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

  public static ISchemaType translate(ThriftType ttype) {
    if (ttype == null) {
      throw new IllegalArgumentException("ThriftType cannot be null");
    }
    switch (ttype.getType()) {
    case ID:
      return ReferenceSchemaType.referTo(
        ThriftProtocolType.UNKNOWN, // TODO: this will definitely need work
        ttype.getTypeModule(), 
        ttype.getTypeId()
      );
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
}
