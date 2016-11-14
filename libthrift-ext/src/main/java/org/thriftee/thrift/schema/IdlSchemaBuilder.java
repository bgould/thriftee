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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;

import org.thriftee.thrift.schema.AbstractFieldSchema.AbstractFieldBuilder;
import org.thriftee.thrift.schema.AbstractFieldSchema.Requiredness;
import org.thriftee.thrift.schema.AbstractStructSchema.AbstractStructSchemaBuilder;
import org.thriftee.thrift.schema.ThriftSchema.Builder;
import org.thriftee.thrift.schema.idl.IdlAnnotation;
import org.thriftee.thrift.schema.idl.IdlBodyDefinition;
import org.thriftee.thrift.schema.idl.IdlConst;
import org.thriftee.thrift.schema.idl.IdlDocument;
import org.thriftee.thrift.schema.idl.IdlEnum;
import org.thriftee.thrift.schema.idl.IdlEnumMember;
import org.thriftee.thrift.schema.idl.IdlException;
import org.thriftee.thrift.schema.idl.IdlField;
import org.thriftee.thrift.schema.idl.IdlHeaderDefinition;
import org.thriftee.thrift.schema.idl.IdlInclude;
import org.thriftee.thrift.schema.idl.IdlMethod;
import org.thriftee.thrift.schema.idl.IdlNamespace;
import org.thriftee.thrift.schema.idl.IdlSchema;
import org.thriftee.thrift.schema.idl.IdlService;
import org.thriftee.thrift.schema.idl.IdlStruct;
import org.thriftee.thrift.schema.idl.IdlType;
import org.thriftee.thrift.schema.idl.IdlTypedef;
import org.thriftee.thrift.schema.idl.IdlUnion;

/**
 * <p>Builds a {@link ThriftSchema} from a deserialized Thrift IDL model.</p>
 * @author bcg
 */
public final class IdlSchemaBuilder {

  public synchronized ThriftSchema buildFrom(final IdlSchema model)
      throws SchemaBuilderException {
    try {
      this.schemaBuilder = new Builder().name("ThriftEE");
      this.model = model;
      return translate();
    } finally {
      this.model = null;
      this.schemaBuilder = null;
    }
  }

  public synchronized ThriftSchema buildFromXml(final Source source)
      throws SchemaBuilderException {
    return buildFrom(IdlXmlUtils.fromXml(source));
  }

  private IdlSchema model;

  private ThriftSchema.Builder schemaBuilder;

  protected ThriftSchema translate() throws SchemaBuilderException {
    for (final IdlDocument document : this.model.getDocuments()) {
      translate(schemaBuilder, document);
    }
    return schemaBuilder.build();
  }

  protected ModuleSchema.Builder translate(
        final ThriftSchema.Builder parentBuilder, final IdlDocument doc
      ) throws SchemaBuilderException {
    final ModuleSchema.Builder val = parentBuilder.addModule(doc.getName());
    val.doc(doc.getDoc());
    final Set<String> includes = new LinkedHashSet<>();
    String xmlTargetNamespace = null;
    String starXmlTargetNamespace = null;
    for (final IdlHeaderDefinition def : doc.getHeader()) {
      final Object obj = def.getFieldValue();
      if (obj instanceof IdlInclude) {
        final IdlInclude include = (IdlInclude) obj;
        includes.add(include.getName());
      } else if (obj instanceof IdlNamespace) {
        // TODO: add support for namespaces
        final IdlNamespace namespace = (IdlNamespace) obj;
        if ("*".equals(namespace.getName())) {
          for (IdlAnnotation annot : namespace.annotations) {
            if ("xml.targetNamespace".equals(annot.getKey())) {
              starXmlTargetNamespace = trimToNull(annot.getValue());
              break;
            }
          }
        } else if ("xml".equals(namespace.getName())) {
          for (IdlAnnotation annot : namespace.annotations) {
            if ("targetNamespace".equals(annot.getKey())) {
              xmlTargetNamespace = trimToNull(annot.getValue());
              break;
            }
          }
        }
      } else {
        throw new SchemaBuilderException("Unhandled header type: " + obj);
      }
    }
    if (xmlTargetNamespace != null) {
      val.xmlTargetNamespace(xmlTargetNamespace);
    } else if (starXmlTargetNamespace != null) {
      val.xmlTargetNamespace(starXmlTargetNamespace);
    }
    val.addIncludes(includes);
    for (final IdlBodyDefinition bodyDefinition : doc.getDefinitions()) {
      final Object definition = bodyDefinition.getFieldValue();
      if (definition instanceof IdlService) {
        translate(val, (IdlService) definition);
      } else if (definition instanceof IdlStruct) {
        translate(val, (IdlStruct) definition);
      } else if (definition instanceof IdlUnion) {
        translate(val, (IdlUnion) definition);
      } else if (definition instanceof IdlEnum) {
        translate(val, (IdlEnum) definition);
      } else if (definition instanceof IdlException) {
        translate(val, (IdlException) definition);
      } else if (definition instanceof IdlConst) {
        continue; // TODO: implement
      } else if (definition instanceof IdlTypedef) {
        translate(val, (IdlTypedef) definition);
      } else {
        throw new SchemaBuilderException(
          String.format("Type cannot be null for `%s`", definition)
        );
      }
    }
    return val;
  }

  protected ExceptionSchema.Builder translate(
        final ModuleSchema.Builder parentBuilder,
        final IdlException exception
      ) throws SchemaBuilderException {
    final ExceptionSchema.Builder val = parentBuilder.addException(
      exception.getName()
    );
    val.doc(exception.getDoc());
    if (exception.getAnnotations() != null) {
      for (final IdlAnnotation annotation : exception.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<IdlField> fields = exception.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final IdlField field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected UnionSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final IdlUnion union) throws SchemaBuilderException {
    final UnionSchema.Builder val = parentBuilder.addUnion(union.getName());
    val.doc(union.getDoc());
    if (union.getAnnotations() != null) {
      for (final IdlAnnotation annotation : union.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<IdlField> fields = union.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final IdlField field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected StructSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final IdlStruct struct) throws SchemaBuilderException {
    final StructSchema.Builder val = parentBuilder.addStruct(struct.getName());
    val.doc(struct.getDoc());
    if (struct.getAnnotations() != null) {
      for (final IdlAnnotation annotation : struct.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<IdlField> fields = struct.getFields();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final IdlField field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected EnumSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final IdlEnum thriftEnum) {
    final EnumSchema.Builder val = parentBuilder.addEnum(thriftEnum.getName());
    val.doc(thriftEnum.getDoc());
    if (thriftEnum.getAnnotations() != null) {
      for (final IdlAnnotation annotation : thriftEnum.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<IdlEnumMember> fields = thriftEnum.getMembers();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final IdlEnumMember field = fields.get(i);
      translate(val, field);
    }
    return val;
  }

  protected EnumValueSchema.Builder translate(
      final EnumSchema.Builder parent, final IdlEnumMember field) {
    final EnumValueSchema.Builder val =
        parent.addEnumValue(field.getName(), field.getValue());
    val.doc(field.getDoc());
    if (field.getAnnotations() != null) {
      for (final IdlAnnotation annotation : field.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    return val;
  }

  protected TypedefSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final IdlTypedef typedef) throws SchemaBuilderException {
    TypedefSchema.Builder val = parentBuilder.addTypedef(typedef.getName());
    val.type(translate(typedef.getType()));
    return val;
  }

  protected ServiceSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final IdlService service) throws SchemaBuilderException {
    ServiceSchema.Builder val = parentBuilder.addService(service.getName());
    val.doc(service.getDoc());
    if (service.getAnnotations() != null) {
      for (final IdlAnnotation annotation : service.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    if (service.getParentId() != null || service.getParentModule() != null) {
      val.parentService(service.getParentModule()+"."+service.getParentId());
    }
    final List<IdlMethod> methods = service.getMethods();
    for (int i = 0, c = methods.size(); i < c; i++) {
      IdlMethod method = methods.get(i);
      translate(val, method);
    }
    return val;
  }

  protected MethodSchema.Builder translate(
        final ServiceSchema.Builder bldr,
        final IdlMethod method
      ) throws SchemaBuilderException {
    MethodSchema.Builder val = bldr.addMethod(method.getName())
                                   .oneway(method.isIsOneway())
                                   .returnType(translate(method.getReturns()))
                                   .doc(method.getDoc());
    if (method.getAnnotations() != null) {
      for (final IdlAnnotation annotation : method.getAnnotations()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<IdlField> arguments = method.getArguments();
    for (int i = 0, c = arguments.size(); i < c; i++) {
      final IdlField field = arguments.get(i);
      translateArgument(val, field);
    }
    final List<IdlField> exceptions = method.getExceptions();
    for (int i = 0, c = exceptions.size(); i < c; i++) {
      final IdlField field = exceptions.get(i);
      translateThrows(val, field);
    }
    return val;
  }

  protected <B extends AbstractFieldBuilder<?, ?, PB, B>,
            PB extends AbstractStructSchemaBuilder<?, ?, ?, B, PB>>
      B translateField(PB parent, IdlField field) throws SchemaBuilderException {
    B newfield = parent.addField(field.getName());
    _translate(newfield, field);
    return newfield;
  }

  protected MethodThrowsSchema.Builder translateThrows(
      MethodSchema.Builder parentBuilder,
      IdlField field ) throws SchemaBuilderException {
    MethodThrowsSchema.Builder exc = parentBuilder.addThrows(field.getName());
    _translate(exc, field);
    return exc;
  }

  protected MethodArgSchema.Builder translateArgument(
      MethodSchema.Builder parentBuilder,
      IdlField field) throws SchemaBuilderException {
    MethodArgSchema.Builder arg = parentBuilder.addArgument(field.getName());
    _translate(arg, field);
    return arg;
  }

  protected <T extends AbstractFieldBuilder<?, ?, ?, ?>> T _translate(
        final T arg,
        final IdlField field
      ) throws SchemaBuilderException {
    arg.identifier(field.getFieldId());
    if (field.getRequiredness() != null) {
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
    } else {
      arg.requiredness(null);
    }
    arg.type(translate(field.getType()));
    return arg;
  }

  protected SchemaType translate(final IdlType ttype)
      throws SchemaBuilderException {
    if (ttype == null) {
      throw new IllegalArgumentException("ThriftType cannot be null");
    }
    switch (ttype.getType()) {
    case ENUM:
    case UNION:
    case STRUCT:
    case TYPEDEF:
    case EXCEPTION:
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
    case I8:
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
    case VOID:
      return PrimitiveTypeSchema.VOID;
    default:
      throw new SchemaBuilderException("unknown ThriftType: " + ttype.getType());
    }
  }

  private SchemaReference.Type resolveSchemaReferenceType(IdlType type)
      throws SchemaBuilderException {
    switch (type.getType()) {
    case ENUM:
      return SchemaReference.Type.ENUM;
    case UNION:
      return SchemaReference.Type.UNION;
    case STRUCT:
      return SchemaReference.Type.STRUCT;
    case TYPEDEF:
      return SchemaReference.Type.TYPEDEF;
    case EXCEPTION:
      return SchemaReference.Type.EXCEPTION;
    default:
      throw new SchemaBuilderException(
        "could not find type: " + type.getTypeModule()+"."+type.getTypeId()
      );
    }
  }

  public static final String trimToNull(String s) {
    if (s == null) return null;
    final String trimmed = s.trim();
    return trimmed.length() == 0 ? null : trimmed;
  }
}
