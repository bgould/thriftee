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
package org.thriftee.provider.swift;

import java.util.List;

import org.thriftee.compiler.schema.AbstractFieldSchema.AbstractFieldBuilder;
import org.thriftee.compiler.schema.AbstractFieldSchema.Requiredness;
import org.thriftee.compiler.schema.AbstractStructSchema.AbstractStructSchemaBuilder;
import org.thriftee.compiler.schema.EnumSchema;
import org.thriftee.compiler.schema.EnumValueSchema;
import org.thriftee.compiler.schema.ExceptionSchema;
import org.thriftee.compiler.schema.ISchemaType;
import org.thriftee.compiler.schema.ListSchemaType;
import org.thriftee.compiler.schema.MapSchemaType;
import org.thriftee.compiler.schema.MethodArgumentSchema;
import org.thriftee.compiler.schema.MethodSchema;
import org.thriftee.compiler.schema.MethodThrowsSchema;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.PrimitiveTypeSchema;
import org.thriftee.compiler.schema.ReferenceSchemaType;
import org.thriftee.compiler.schema.SchemaBuilderException;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.schema.SetSchemaType;
import org.thriftee.compiler.schema.StructSchema;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.compiler.schema.UnionSchema;

import com.facebook.swift.codec.ThriftProtocolType;
import com.facebook.swift.parser.model.BaseType;
import com.facebook.swift.parser.model.Definition;
import com.facebook.swift.parser.model.Document;
import com.facebook.swift.parser.model.IdentifierType;
import com.facebook.swift.parser.model.IntegerEnum;
import com.facebook.swift.parser.model.IntegerEnumField;
import com.facebook.swift.parser.model.ListType;
import com.facebook.swift.parser.model.MapType;
import com.facebook.swift.parser.model.Service;
import com.facebook.swift.parser.model.SetType;
import com.facebook.swift.parser.model.Struct;
import com.facebook.swift.parser.model.ThriftException;
import com.facebook.swift.parser.model.ThriftField;
import com.facebook.swift.parser.model.ThriftMethod;
import com.facebook.swift.parser.model.ThriftType;
import com.facebook.swift.parser.model.Union;
import com.facebook.swift.parser.model.VoidType;

public class SwiftTranslator {

  public static ModuleSchema.Builder translate(
        final ThriftSchema.Builder parentBuilder, 
        final String _name, 
        final Document _document
      ) throws SchemaBuilderException {
    final ModuleSchema.Builder val = parentBuilder.addModule(_name);
    val.addIncludes(_document.getHeader().getIncludes());
    final List<Definition> definitions = _document.getDefinitions();
    for (final Definition definition : definitions) {
      if (definition instanceof Service) {
        translate(val, (Service) definition);
      } else if (definition instanceof Struct) {
        translate(val, (Struct) definition);
      } else if (definition instanceof Union) {
        translate(val, (Union) definition);
      } else if (definition instanceof IntegerEnum) {
        translate(val, (IntegerEnum) definition);
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
    final List<ThriftField> fields = _exception.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final ThriftField field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  public static UnionSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Union _union) {
    final UnionSchema.Builder val = parentBuilder.addUnion(_union.getName());
    final List<ThriftField> fields = _union.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final ThriftField field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  public static StructSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Struct _struct) {
    final StructSchema.Builder val = parentBuilder.addStruct(_struct.getName());
    final List<ThriftField> fields = _struct.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final ThriftField field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  public static EnumSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final IntegerEnum _enum) {
    EnumSchema.Builder val = parentBuilder.addEnum(_enum.getName());
    final List<IntegerEnumField> fields = _enum.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      IntegerEnumField field = fields.get(i);
      translate(val, field);
    }
    return val;
  }

  public static EnumValueSchema.Builder translate(
      final EnumSchema.Builder parentBuilder, 
      final IntegerEnumField _field) {
    EnumValueSchema.Builder val = parentBuilder.addEnumValue(_field.getName());
    if (_field.getExplicitValue().isPresent()) {
      // TODO: support explicit values for enum fields in schema model
      val.explicitValue(_field.getExplicitValue().get());
    }
    return val;
  }

  public static ServiceSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Service _service) {
    ServiceSchema.Builder val = parentBuilder.addService(_service.getName());
    if (_service.getParent().isPresent()) {
      val.parentService(_service.getParent().get());
    }
    final List<ThriftMethod> methods = _service.getMethods();
    for (int i = 0, c = methods.size(); i < c; i++) {
      ThriftMethod method = methods.get(i);
      translate(val, method);
    }
    return val;
  }

  public static MethodSchema.Builder translate(
      ServiceSchema.Builder parentBuilder, 
      ThriftMethod _method) {
    final MethodSchema.Builder val = parentBuilder
        .addMethod(_method.getName())
        .oneway(_method.isOneway())
        .returnType(translate(_method.getReturnType()));
    final List<ThriftField> arguments = _method.getArguments(); 
    for (int i = 0, c = arguments.size(); i < c; i++) {
      ThriftField field = arguments.get(i);
      translateArgument(val, field);
    }
    final List<ThriftField> exceptions = _method.getThrowsFields(); 
    for (int i = 0, c = exceptions.size(); i < c; i++) {
      ThriftField field = exceptions.get(i);
      translateThrows(val, field);
    }
    return val;
  }

  public static <B extends AbstractFieldBuilder<?, ?, PB, B>, 
          PB extends AbstractStructSchemaBuilder<?, ?, ?, B, PB>> 
      B translateField(PB parentBuilder, ThriftField _field) {
    B field = parentBuilder.addField(_field.getName());
    _translate(field, _field);
    return field;
  }

  /*
  public static StructFieldSchema.Builder translateField(StructSchema.Builder parentBuilder, ThriftField _field) {
    StructFieldSchema.Builder field = parentBuilder.addField(_field.getName());
    _translate(field, _field);
    return field;
  }
  
  public static UnionFieldSchema.Builder translateField(UnionSchema.Builder parentBuilder, ThriftField _field) {
    UnionFieldSchema.Builder field = parentBuilder.addField(_field.getName());
    _translate(field, _field);
    return field;
  }
  */

  public static MethodThrowsSchema.Builder translateThrows(MethodSchema.Builder parentBuilder, ThriftField field) {
    MethodThrowsSchema.Builder exc = parentBuilder.addThrows(field.getName());
    _translate(exc, field);
    return exc;
  }

  public static MethodArgumentSchema.Builder translateArgument(MethodSchema.Builder parentBuilder, ThriftField field) {
    MethodArgumentSchema.Builder arg = parentBuilder.addArgument(field.getName());
    _translate(arg, field);
    return arg;
  }

  private static <T extends AbstractFieldBuilder<?, ?, ?, ?>> T _translate(T arg, ThriftField field) {
    if (field.getIdentifier().isPresent()) {
      arg.identifier(field.getIdentifier().get());
    }
    switch (field.getRequiredness()) {
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
    arg.type(translate(field.getType()));
    return arg;
  }

  public static ISchemaType translate(ThriftType thriftType) {
    if (thriftType instanceof IdentifierType) {
      // TODO: this will definitely need work
      return ReferenceSchemaType.referTo(
        ThriftProtocolType.UNKNOWN, 
        null, 
        ((IdentifierType) thriftType).getName()
      );
    } else if (thriftType instanceof MapType) {
      return new MapSchemaType(
        translate(((MapType) thriftType).getKeyType()), 
        translate(((MapType) thriftType).getValueType())
      );
    } else if (thriftType instanceof ListType) {
      return new ListSchemaType(
        translate(((ListType) thriftType).getElementType())
      );
    } else if (thriftType instanceof SetType) {
      return new SetSchemaType(
        translate(((SetType) thriftType).getElementType())
      );
    } else if (thriftType instanceof BaseType) {
      switch (((BaseType) thriftType).getType()) {
      case BOOL:
        return PrimitiveTypeSchema.BOOL;
      case BYTE:
        return PrimitiveTypeSchema.BYTE;
      case I16:
        return PrimitiveTypeSchema.I16;
      case I32:
        return PrimitiveTypeSchema.I32;
      case I64:
        return PrimitiveTypeSchema.I64;
      case DOUBLE:
        return PrimitiveTypeSchema.DOUBLE;
      case STRING:
        return PrimitiveTypeSchema.STRING;
      case BINARY:
        return PrimitiveTypeSchema.BINARY;
      default:
        throw new IllegalStateException(
          "unknown BaseType: " + ((BaseType) thriftType).getType());
      }
    } else if (thriftType instanceof VoidType) {
      return PrimitiveTypeSchema.VOID;
    } else {
      throw new IllegalStateException(
          "unhandled type: " + thriftType.toString());
    }
  }

}
