package org.thriftee.compiler.schema;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface ThriftSchemaService {

  @ThriftMethod
  public ThriftSchema getSchema();

  public static class Impl implements ThriftSchemaService {
    private final ThriftSchema schema;
    public Impl(final ThriftSchema schema) {
      this.schema = schema;
    }
    public ThriftSchema getSchema() {
      return schema;
    }
  }

}
