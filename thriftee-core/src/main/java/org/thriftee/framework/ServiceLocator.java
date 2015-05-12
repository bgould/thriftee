package org.thriftee.framework;

public interface ServiceLocator {

  public <I> I locate(Class<I> svcIntf) throws ServiceLocatorException;

}
