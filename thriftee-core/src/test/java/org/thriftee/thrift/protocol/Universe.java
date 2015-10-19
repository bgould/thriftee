package org.thriftee.thrift.protocol;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface Universe {

  @ThriftMethod
  public int grok(@ThriftField Everything everything);

}
