namespace * org.thriftee.meta.idl

include "org.thriftee.compiler.idl.thrift"

service ThriftSchemaService {

  org.thriftee.compiler.idl.IdlSchema getSchema();

}