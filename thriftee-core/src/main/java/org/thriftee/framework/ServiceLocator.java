package org.thriftee.framework;

public interface ServiceLocator {

  public <I> void register(Class<I> i, I svc) throws ServiceLocatorException;

  public <I> I locate(Class<I> svcIntf) throws ServiceLocatorException;

}
