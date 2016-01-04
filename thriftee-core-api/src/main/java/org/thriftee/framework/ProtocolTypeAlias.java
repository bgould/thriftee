package org.thriftee.framework;

import org.apache.thrift.protocol.TProtocolFactory;

public interface ProtocolTypeAlias {

  String getName();

  TProtocolFactory getInFactory();

  TProtocolFactory getOutFactory();

}