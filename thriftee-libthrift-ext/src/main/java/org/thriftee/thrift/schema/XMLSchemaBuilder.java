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
 *//*
package org.thriftee.compiler.schema;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.thriftee.compiler.schema.AbstractFieldSchema.AbstractFieldBuilder;
import org.thriftee.compiler.schema.AbstractFieldSchema.Requiredness;
import org.thriftee.compiler.schema.AbstractStructSchema.AbstractStructSchemaBuilder;
import org.thriftee.compiler.schema.SchemaReference.Type;
import org.thriftee.compiler.schema.ThriftSchema.Builder;
import org.thriftee.thrift.xml.idl.Annotation;
import org.thriftee.thrift.xml.idl.Const;
import org.thriftee.thrift.xml.idl.Document;
import org.thriftee.thrift.xml.idl.Field;
import org.thriftee.thrift.xml.idl.IDL;
import org.thriftee.thrift.xml.idl.Include;
import org.thriftee.thrift.xml.idl.Method;
import org.thriftee.thrift.xml.idl.Namespace;
import org.thriftee.thrift.xml.idl.Service;
import org.thriftee.thrift.xml.idl.Struct;
import org.thriftee.thrift.xml.idl.ThriftEnum;
import org.thriftee.thrift.xml.idl.ThriftEnum.Member;
import org.thriftee.thrift.xml.idl.ThriftException;
import org.thriftee.thrift.xml.idl.ThriftType;
import org.thriftee.thrift.xml.idl.Typedef;
import org.thriftee.thrift.xml.idl.Union;
*/
/**
 * <p>Builds a {@link ThriftSchema} from a JAXB model of the output from the
 * Thrift compiler's XML generator.</p>
 * @author bcg
 *//*
public final class XMLSchemaBuilder {

  public synchronized ThriftSchema buildSchema(final File xmlModelFile)
      throws SchemaBuilderException {
    try {
      this.model = JAXB.unmarshal(xmlModelFile, IDL.class);
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
    val.doc(doc.getDoc());
    final Set<String> includes = new LinkedHashSet<>();
    for (final Object obj : doc.getIncludeOrNamespace()) {
      if (obj instanceof Include) {
        final Include include = (Include) obj;
        includes.add(include.getName());
      } else if (obj instanceof Namespace) {
        // TODO: add support for namespaces
      } else {
        throw new SchemaBuilderException("Unhandled header type: " + obj);
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
          String.format("Type cannot be null for `%s`", definition.getClass())
        );
      }
    }
    return val;
  }

  protected ExceptionSchema.Builder translate(
        final ModuleSchema.Builder parentBuilder, 
        final ThriftException exception
      ) throws SchemaBuilderException {
    final ExceptionSchema.Builder val = parentBuilder.addException(
      exception.getName()
    );
    val.doc(exception.getDoc());
    if (exception.getAnnotation() != null) {
      for (final Annotation annotation : exception.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<Field> fields = exception.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }
  
  protected UnionSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder,
      final Union union) throws SchemaBuilderException {
    final UnionSchema.Builder val = parentBuilder.addUnion(union.getName());
    val.doc(union.getDoc());
    if (union.getAnnotation() != null) {
      for (final Annotation annotation : union.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<Field> fields = union.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected StructSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final Struct struct) throws SchemaBuilderException {
    final StructSchema.Builder val = parentBuilder.addStruct(struct.getName());
    val.doc(struct.getDoc());
    if (struct.getAnnotation() != null) {
      for (final Annotation annotation : struct.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<Field> fields = struct.getField();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Field field = fields.get(i);
      translateField(val, field);
    }
    return val;
  }

  protected EnumSchema.Builder translate(
      final ModuleSchema.Builder parentBuilder, 
      final ThriftEnum thriftEnum) {
    final EnumSchema.Builder val = parentBuilder.addEnum(thriftEnum.getName());
    val.doc(thriftEnum.getDoc());
    if (thriftEnum.getAnnotation() != null) {
      for (final Annotation annotation : thriftEnum.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<Member> fields = thriftEnum.getMember();
    for (int i = 0, c = fields.size(); i < c; i++) {
      final Member field = fields.get(i);
      translate(val, field);
    }
    return val;
  }

  protected EnumValueSchema.Builder translate(
      final EnumSchema.Builder parent, final Member field) {
    final EnumValueSchema.Builder val = 
        parent.addEnumValue(field.getName(), field.getValue());
    val.doc(field.getDoc());
    if (field.getAnnotation() != null) {
      for (final Annotation annotation : field.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
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
      final Service service) throws SchemaBuilderException {
    ServiceSchema.Builder val = parentBuilder.addService(service.getName());
    val.doc(service.getDoc());
    if (service.getAnnotation() != null) {
      for (final Annotation annotation : service.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    if (service.getParentId() != null || service.getParentModule() != null) {
      val.parentService(service.getParentModule()+"."+service.getParentId());
    }
    final List<Method> methods = service.getMethod();
    for (int i = 0, c = methods.size(); i < c; i++) {
      Method method = methods.get(i);
      translate(val, method);
    }
    return val;
  }

  protected MethodSchema.Builder translate(
        final ServiceSchema.Builder bldr, 
        final Method method
      ) throws SchemaBuilderException {
    boolean oneway = method.isOneway() == null ? false : method.isOneway();
    MethodSchema.Builder val = bldr.addMethod(method.getName())
                                   .oneway(oneway)
                                   .returnType(translate(method.getReturns()))
                                   .doc(method.getDoc());
    if (method.getAnnotation() != null) {
      for (final Annotation annotation : method.getAnnotation()) {
        val.addAnnotation(annotation.getKey(), annotation.getValue());
      }
    }
    final List<Field> arguments = method.getArg(); 
    for (int i = 0, c = arguments.size(); i < c; i++) {
      final Field field = arguments.get(i);
      translateArgument(val, field);
    }
    final List<Field> exceptions = method.getThrows(); 
    for (int i = 0, c = exceptions.size(); i < c; i++) {
      final Field field = exceptions.get(i);
      translateThrows(val, field);
    }
    return val;
  }
  
  protected <B extends AbstractFieldBuilder<?, ?, PB, B>, 
            PB extends AbstractStructSchemaBuilder<?, ?, ?, B, PB>> 
      B translateField(PB parent, Field field) throws SchemaBuilderException {
    B newfield = parent.addField(field.getName());
    _translate(newfield, field);
    return newfield;
  }

  protected MethodThrowsSchema.Builder translateThrows(
      MethodSchema.Builder parentBuilder, 
      Field field ) throws SchemaBuilderException {
    MethodThrowsSchema.Builder exc = parentBuilder.addThrows(field.getName());
    _translate(exc, field);
    return exc;
  }

  protected MethodArgSchema.Builder translateArgument(
      MethodSchema.Builder parentBuilder, 
      Field field) throws SchemaBuilderException {
    MethodArgSchema.Builder arg = parentBuilder.addArgument(field.getName());
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

  protected SchemaType translate(final ThriftType ttype)
      throws SchemaBuilderException {
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

  private Document resolveDocument(String name) {
    for (final Document doc : model.getDocument()) {
      if (name.equals(doc.getName())) {
        return doc;
      }
    }
    throw new IllegalArgumentException("could not find document: " + name);
  }

}
*/