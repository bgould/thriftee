package org.thriftee.compiler.schema;

import org.thriftee.framework.ThriftEE;

public interface SchemaBuilder {

    public abstract ThriftSchema buildSchema(ThriftEE thrift)
            throws SchemaBuilderException;

}