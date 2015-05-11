package org.thriftee.framework;

import org.apache.thrift.protocol.TProtocolFactory;

public class ProtocolTypeAlias {

  private final String name;

  private final TProtocolFactory inFactory;

  private final TProtocolFactory outFactory;

  public ProtocolTypeAlias(
      final String _name, 
      final TProtocolFactory _factory
    ) {
    this(_name, _factory, _factory);
  }

  public ProtocolTypeAlias(
      final String _name, 
      final TProtocolFactory _inFactory, 
      final TProtocolFactory _outFactory
    ) {
    if (_name == null) {
      throw new IllegalArgumentException("name is required.");
    }
    if (_inFactory == null) {
      throw new IllegalArgumentException("inFactory is required.");
    }
    if (_outFactory == null) {
      throw new IllegalArgumentException("outFactory is required.");
    }
    this.name = _name;
    this.inFactory = _inFactory;
    this.outFactory = _outFactory;
  }

  public String getName() {
    return name;
  }

  public TProtocolFactory getInFactory() {
    return inFactory;
  }

  public TProtocolFactory getOutFactory() {
    return outFactory;
  }

}
