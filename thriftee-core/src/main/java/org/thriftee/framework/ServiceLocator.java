package org.thriftee.framework;

public interface ServiceLocator {

  public <I> I locate(
    ThriftEE thriftee, 
    Class<I> svcIntf
  ) throws ServiceLocatorException;

}
