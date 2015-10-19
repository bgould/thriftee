package org.thriftee.compiler.schema;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface ThriftSchemaService {

  @ThriftMethod
  public ThriftSchema getSchema();
  
  @ThriftMethod
  public String xmlTemplate(@ThriftField(name="type") ThriftSchemaType type);

  @ThriftMethod
  public String xmlCall(@ThriftField(name="methodId") MethodIdentifier method);

  public static class Impl implements ThriftSchemaService {

    private final ThriftSchema schema;

    public Impl(final ThriftSchema schema) {
      this.schema = schema;
    }

    @Override
    public ThriftSchema getSchema() {
      return schema;
    }

    @Override
    public String xmlTemplate(ThriftSchemaType type) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String xmlCall(MethodIdentifier method) {
      // TODO Auto-generated method stub
      return null;
    }

  }

}
