namespace * org.thriftee.meta.idl

include "org.thriftee.thrift.schema.idl.thrift"

service ThriftSchemaService {

  org.thriftee.thrift.schema.idl.IdlSchema getSchema();

}