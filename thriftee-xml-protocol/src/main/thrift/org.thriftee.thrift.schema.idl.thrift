namespace * org.thriftee.thrift.schema.idl

enum IdlRequiredness {
  REQUIRED, OPTIONAL, NONE
}

enum IdlTypeIdentifier {
  VOID,
  BOOL,
  I8,
  I16,
  I32,
  I64,
  DOUBLE,
  BINARY,
  STRING,
  MAP,
  SET,
  LIST,
  ENUM,
  UNION,
  STRUCT,
  TYPEDEF,
  EXCEPTION
}

struct IdlAnnotation {
  1: required string key;
  2: optional string value;
}

struct IdlSchema {
  1: list<IdlDocument> documents;
}

struct IdlDocument {
  1: required string name;
  2: optional string doc;
  3: list<IdlHeaderDefinition> header;
  4: list<IdlBodyDefinition> definitions;
}

union IdlHeaderDefinition {
  1: IdlInclude includeDef;
  2: IdlNamespace namespaceDef;
}

union IdlBodyDefinition {
  1: IdlEnum enumDef;
  2: IdlConst constDef;
  3: IdlUnion unionDef;
  4: IdlStruct structDef;
  5: IdlTypedef typedefDef;
  6: IdlService serviceDef;
  7: IdlException exceptionDef;
}

struct IdlInclude {
  1: required string name;
  2: optional string file;
}

struct IdlNamespace {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: required string value;
}

struct IdlEnum {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: required list<IdlEnumMember> members;
}

struct IdlEnumMember {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: required i32 value;
}

struct IdlConst {
  1: required string name;
  2: optional string doc;
  3: required IdlConstValue value;
}

union IdlConstValue {
  1: string stringValue;
  2: double doubleValue;
  3: list<IdlConstValue> listValue;
  4: map<IdlConstValue, IdlConstValue> mapValue;
  5: i64 longValue;
}

struct IdlUnion {
  1: required string name;
  2: optional string doc;
  3: list<IdlAnnotation> annotations;
  4: list<IdlField> fields;
}

struct IdlStruct {
  1: required string name;
  2: optional string doc;
  3: list<IdlAnnotation> annotations;
  4: list<IdlField> fields;
}

struct IdlTypedef {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: required IdlType type;
}

struct IdlException {
  1: required string name;
  2: optional string doc;
  3: list<IdlAnnotation> annotations;
  4: list<IdlField> fields;
}

struct IdlField {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: required i16 fieldId;
  5: required IdlRequiredness requiredness;
  6: required IdlType type;
  7: optional IdlConstValue defaultValue;
}

struct IdlService {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: optional string parentModule;
  5: optional string parentId;
  6: optional string targetNamespace;
  7: list<IdlMethod> methods;
}

struct IdlMethod {
  1: required string name;
  2: optional string doc;
  3: optional list<IdlAnnotation> annotations;
  4: required bool isOneway;
  5: required IdlType returns;
  6: list<IdlField> arguments;
  7: list<IdlField> exceptions;
}

struct IdlType {
  1: required IdlTypeIdentifier type;
  2: optional string typeModule;
  3: optional string typeId;
  4: optional IdlType elemType;
  5: optional IdlType keyType;
  6: optional IdlType valueType;
}
