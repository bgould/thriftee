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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;
import javax.xml.transform.Source;

import org.thriftee.thrift.schema.idl.IdlAnnotation;
import org.thriftee.thrift.schema.idl.IdlBodyDefinition;
import org.thriftee.thrift.schema.idl.IdlDocument;
import org.thriftee.thrift.schema.idl.IdlEnum;
import org.thriftee.thrift.schema.idl.IdlEnumMember;
import org.thriftee.thrift.schema.idl.IdlException;
import org.thriftee.thrift.schema.idl.IdlField;
import org.thriftee.thrift.schema.idl.IdlHeaderDefinition;
import org.thriftee.thrift.schema.idl.IdlInclude;
import org.thriftee.thrift.schema.idl.IdlMethod;
import org.thriftee.thrift.schema.idl.IdlNamespace;
import org.thriftee.thrift.schema.idl.IdlRequiredness;
import org.thriftee.thrift.schema.idl.IdlSchema;
import org.thriftee.thrift.schema.idl.IdlService;
import org.thriftee.thrift.schema.idl.IdlStruct;
import org.thriftee.thrift.schema.idl.IdlType;
import org.thriftee.thrift.schema.idl.IdlTypeIdentifier;
import org.thriftee.thrift.schema.idl.IdlTypedef;
import org.thriftee.thrift.schema.idl.IdlUnion;
import org.thriftee.thrift.schema.xml.XmlThriftAnnotation;
import org.thriftee.thrift.schema.xml.XmlThriftConst;
import org.thriftee.thrift.schema.xml.XmlThriftDocument;
import org.thriftee.thrift.schema.xml.XmlThriftEnum;
import org.thriftee.thrift.schema.xml.XmlThriftEnum.XmlThriftMember;
import org.thriftee.thrift.schema.xml.XmlThriftException;
import org.thriftee.thrift.schema.xml.XmlThriftField;
import org.thriftee.thrift.schema.xml.XmlThriftIDL;
import org.thriftee.thrift.schema.xml.XmlThriftInclude;
import org.thriftee.thrift.schema.xml.XmlThriftMethod;
import org.thriftee.thrift.schema.xml.XmlThriftNamespace;
import org.thriftee.thrift.schema.xml.XmlThriftService;
import org.thriftee.thrift.schema.xml.XmlThriftStruct;
import org.thriftee.thrift.schema.xml.XmlThriftType;
import org.thriftee.thrift.schema.xml.XmlThriftTypedef;
import org.thriftee.thrift.schema.xml.XmlThriftUnion;

/**
 * @author bcg
 */
public final class IdlXmlUtils {

  public static IdlSchema fromXml(Source src) throws SchemaBuilderException {
    return new Read(src).fromXml();
  }

  // singleton utility class
  private IdlXmlUtils() {}

  private static SchemaBuilderException ex(String fmt, Object... args) {
    return new SchemaBuilderException(String.format(fmt, args));
  }

  private static SchemaBuilderException ex(Throwable t, String msg) {
    return new SchemaBuilderException(msg, t);
  }

  private static final class Read {

    private final XmlThriftIDL model;

    private Read(final Source source) throws SchemaBuilderException {
      try {
        model = JAXB.unmarshal(source, XmlThriftIDL.class);
      } catch (Exception e) {
        throw ex(e, "error parsing input XML");
      }
    }

    private IdlSchema fromXml() throws SchemaBuilderException {
      final IdlSchema result = new IdlSchema();
      result.setDocuments(new ArrayList<IdlDocument>());
      if (model.getDocument() != null) {
        for (final XmlThriftDocument doc : model.getDocument()) {
          result.getDocuments().add(fromXml(doc));
        }
      }
      return result;
    }

    private IdlDocument fromXml(final XmlThriftDocument doc)
        throws SchemaBuilderException {

      final IdlDocument val = new IdlDocument();
      val.setName(doc.getName());
      val.setDoc(doc.getDoc());

      final List<IdlHeaderDefinition> header = new ArrayList<>();
      for (final Object obj : doc.getIncludeOrNamespace()) {
        final IdlHeaderDefinition def = new IdlHeaderDefinition();
        if (obj instanceof XmlThriftInclude) {
          final XmlThriftInclude include = (XmlThriftInclude) obj;
          final IdlInclude idlInclude = new IdlInclude();
          idlInclude.setName(include.getName());
          def.setIncludeDef(idlInclude);
        } else if (obj instanceof XmlThriftNamespace) {
          final XmlThriftNamespace namespace = (XmlThriftNamespace) obj;
          final IdlNamespace idlNamespace = new IdlNamespace();
          idlNamespace.setName(namespace.getName());
          idlNamespace.setDoc(namespace.getDoc());
          idlNamespace.setAnnotations(fromXml(namespace.getAnnotation()));
          idlNamespace.setValue(namespace.getValue());
          def.setNamespaceDef(idlNamespace);
        } else {
          throw ex("Unhandled header type: %s", obj);
        }
        header.add(def);
      }
      val.setHeader(header);

      final List<IdlBodyDefinition> definitions = new ArrayList<>();
      for (final Object definition : doc.getExceptionOrTypedefOrService()) {
        final IdlBodyDefinition bodyDef = new IdlBodyDefinition();
        if (definition instanceof XmlThriftService) {
          bodyDef.setServiceDef(fromXml((XmlThriftService) definition));
        } else if (definition instanceof XmlThriftStruct) {
          bodyDef.setStructDef(fromXml((XmlThriftStruct) definition));
        } else if (definition instanceof XmlThriftUnion) {
          bodyDef.setUnionDef(fromXml((XmlThriftUnion) definition));
        } else if (definition instanceof XmlThriftEnum) {
          bodyDef.setEnumDef(fromXml((XmlThriftEnum) definition));
        } else if (definition instanceof XmlThriftException) {
          bodyDef.setExceptionDef(fromXml((XmlThriftException) definition));
        } else if (definition instanceof XmlThriftConst) {
          continue; // TODO: implement
        } else if (definition instanceof XmlThriftTypedef) {
          bodyDef.setTypedefDef(fromXml((XmlThriftTypedef) definition));
        } else {
          throw ex("Type cannot be null for `%s`", definition.getClass());
        }
        definitions.add(bodyDef);
      }
      val.setDefinitions(definitions);

      return val;
    }

    private IdlService fromXml(final XmlThriftService service)
        throws SchemaBuilderException {
      final IdlService val = new IdlService();
      val.setName(service.getName());
      val.setDoc(service.getDoc());
      val.setAnnotations(fromXml(service.getAnnotation()));
      val.setParentModule(service.getParentModule());
      val.setParentId(service.getParentId());
      val.setTargetNamespace(service.getTargetNamespace());
      val.setMethods(fromXmlMethods(service.getMethod()));
      return val;
    }

    private List<IdlMethod> fromXmlMethods(List<XmlThriftMethod> methods)
        throws SchemaBuilderException {
      final List<IdlMethod> result = new ArrayList<>();
      if (methods != null) {
        for (final XmlThriftMethod method : methods) {
          final IdlMethod val = new IdlMethod();
          val.setName(method.getName());
          val.setDoc(method.getDoc());
          val.setAnnotations(fromXml(method.getAnnotation()));
          if (method.isOneway() != null) {
            val.setIsOneway(method.isOneway());
          } else {
            val.setIsOneway(false);
          }
          val.setReturns(fromXmlType(method.getReturns()));
          val.setArguments(fromXmlFields(method.getArg()));
          val.setExceptions(fromXmlFields(method.getThrows()));
          result.add(val);
        }
      }
      return result;
    }

    private IdlEnum fromXml(final XmlThriftEnum thriftEnum)
        throws SchemaBuilderException {
      final IdlEnum val = new IdlEnum();
      val.setName(thriftEnum.getName());
      val.setDoc(thriftEnum.getDoc());
      val.setAnnotations(fromXml(thriftEnum.getAnnotation()));
      val.setMembers(fromXmlMembers(thriftEnum.getMember()));
      return val;
    }

    private IdlTypedef fromXml(final XmlThriftTypedef typedef)
        throws SchemaBuilderException {
      final IdlTypedef val = new IdlTypedef();
      val.setName(typedef.getName());
      val.setDoc(typedef.getDoc());
      val.setAnnotations(fromXml(typedef.getAnnotation()));
      val.setType(fromXmlType(typedef));
      return val;
    }

    private IdlException fromXml(final XmlThriftException exception)
        throws SchemaBuilderException {
      final IdlException val = new IdlException();
      val.setName(exception.getName());
      val.setDoc(exception.getDoc());
      val.setAnnotations(fromXml(exception.getAnnotation()));
      val.setFields(fromXmlFields(exception.getField()));
      return val;
    }

    private IdlUnion fromXml(final XmlThriftUnion union)
        throws SchemaBuilderException {
      final IdlUnion val = new IdlUnion();
      val.setName(union.getName());
      val.setDoc(union.getDoc());
      val.setAnnotations(fromXml(union.getAnnotation()));
      val.setFields(fromXmlFields(union.getField()));
      return val;
    }

    private IdlStruct fromXml(final XmlThriftStruct struct)
        throws SchemaBuilderException {
      final IdlStruct val = new IdlStruct();
      val.setName(struct.getName());
      val.setDoc(struct.getDoc());
      val.setAnnotations(fromXml(struct.getAnnotation()));
      val.setFields(fromXmlFields(struct.getField()));
      return val;
    }

    private List<IdlEnumMember> fromXmlMembers(List<XmlThriftMember> fields)
        throws SchemaBuilderException {
      final List<IdlEnumMember> result = new ArrayList<>();
      for (final XmlThriftMember field : fields) {
        final IdlEnumMember idlField = new IdlEnumMember();
        idlField.setName(field.getName());
        idlField.setDoc(field.getDoc());
        idlField.setAnnotations(fromXml(field.getAnnotation()));
        if (field.getValue() == null) {
          throw ex("enum member value cannot be null");
        }
        idlField.setValue(field.getValue());
        result.add(idlField);
      }
      return result;
    }

    private List<IdlField> fromXmlFields(List<XmlThriftField> fields)
        throws SchemaBuilderException {
      final List<IdlField> result = new ArrayList<>();
      for (final XmlThriftField field : fields) {
        final IdlField idlField = new IdlField();
        idlField.setName(field.getName());
        idlField.setDoc(field.getDoc());
        idlField.setAnnotations(fromXml(field.getAnnotation()));
        if (field.getFieldId() != null) {
          idlField.setFieldId(field.getFieldId().shortValue());
        }
        final IdlRequiredness required;
        if (field.getRequired() != null) {
          switch (field.getRequired()) {
            case REQUIRED: required = IdlRequiredness.REQUIRED; break;
            case OPTIONAL: required = IdlRequiredness.OPTIONAL; break;
            default: throw ex("unknown requiredness: %s", field.getRequired());
          }
        } else {
          required = IdlRequiredness.NONE;
        }
        idlField.setRequiredness(required);
        idlField.setType(fromXmlType(field));
        //idlField.setDefaultValue(fromXml(field.getDefault()));
        result.add(idlField);
      }
      return result;
    }

    private List<IdlAnnotation> fromXml(
        final List<XmlThriftAnnotation> annotations) {
      final List<IdlAnnotation> result = new ArrayList<>();
      if (annotations != null) {
        for (final XmlThriftAnnotation annotation : annotations) {
          final IdlAnnotation idlAnnotation = new IdlAnnotation();
          idlAnnotation.setKey(annotation.getKey());
          idlAnnotation.setValue(annotation.getValue());
          result.add(idlAnnotation);
        }
      }
      return result;
    }

    private IdlType fromXmlType(XmlThriftType ttype)
        throws SchemaBuilderException {
      if (ttype == null) {
        throw ex("ThriftType cannot be null");
      }
      final IdlType result = new IdlType();
      switch (ttype.getType()) {
      case ID:
        result.setType(resolveSchemaReferenceType(ttype));
        result.setTypeModule(ttype.getTypeModule());
        result.setTypeId(ttype.getTypeId());
        return result;
      case MAP:
        result.setType(IdlTypeIdentifier.MAP);
        result.setKeyType(fromXmlType(ttype.getKeyType()));
        result.setValueType(fromXmlType(ttype.getValueType()));
        return result;
      case LIST:
        result.setType(IdlTypeIdentifier.LIST);
        result.setElemType(fromXmlType(ttype.getElemType()));
        return result;
      case SET:
        result.setType(IdlTypeIdentifier.SET);
        result.setElemType(fromXmlType(ttype.getElemType()));
        return result;
      case BOOL:
        result.setType(IdlTypeIdentifier.BOOL);
        return result;
      case I_8:
      case BYTE:
        result.setType(IdlTypeIdentifier.I8);
        return result;
      case I_16:
        result.setType(IdlTypeIdentifier.I16);
        return result;
      case I_32:
        result.setType(IdlTypeIdentifier.I32);
        return result;
      case I_64:
        result.setType(IdlTypeIdentifier.I64);
        return result;
      case DOUBLE:
        result.setType(IdlTypeIdentifier.DOUBLE);
        return result;
      case STRING:
        result.setType(IdlTypeIdentifier.STRING);
        return result;
      case BINARY:
        result.setType(IdlTypeIdentifier.BINARY);
        return result;
      case VOID:
        result.setType(IdlTypeIdentifier.VOID);
        return result;
      default:
        throw ex("unknown ThriftType: `%s`", ttype.getType());
      }
    }

    private IdlTypeIdentifier resolveSchemaReferenceType(XmlThriftType ttype)
        throws SchemaBuilderException {
      final XmlThriftDocument doc = resolveDocument(ttype.getTypeModule());
      final String typename = ttype.getTypeId();
      for (final Object def : doc.getExceptionOrTypedefOrService()) {
        final String defname;
        final IdlTypeIdentifier type;
        if (def instanceof XmlThriftStruct) {
          defname = ((XmlThriftStruct)def).getName();
          type = IdlTypeIdentifier.STRUCT;
        } else if (def instanceof XmlThriftUnion) {
          defname = ((XmlThriftUnion)def).getName();
          type = IdlTypeIdentifier.UNION;
        } else if (def instanceof XmlThriftEnum) {
          defname = ((XmlThriftEnum)def).getName();
          type = IdlTypeIdentifier.ENUM;
        } else if (def instanceof XmlThriftException) {
          defname = ((XmlThriftException)def).getName();
          type = IdlTypeIdentifier.EXCEPTION;
        } else if (def instanceof XmlThriftTypedef) {
          defname = ((XmlThriftTypedef)def).getName();
          type = IdlTypeIdentifier.TYPEDEF;
        } else {
          continue;
        }
        if (typename.equals(defname)) {
          return type;
        }
      }
      final String typenm = ttype.getTypeModule() + "." + ttype.getTypeId();
      throw ex("could not find ttype: `%s`", typenm);
    }

    private XmlThriftDocument resolveDocument(String name)
        throws SchemaBuilderException {
      for (final XmlThriftDocument doc : model.getDocument()) {
        if (name.equals(doc.getName())) {
          return doc;
        }
      }
      throw ex("could not find document: `%s`", name);
    }

  }

}
