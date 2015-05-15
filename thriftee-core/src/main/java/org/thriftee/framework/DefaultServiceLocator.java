package org.thriftee.framework;

import java.util.concurrent.ConcurrentHashMap;

import org.thriftee.framework.ServiceLocatorException.Messages;

public class DefaultServiceLocator implements ServiceLocator {

  private final ConcurrentHashMap<Class<?>, Object> services;

  public DefaultServiceLocator() {
    this.services = new ConcurrentHashMap<>();
  }

  public <I> void register(Class<I> i, I svc) throws ServiceLocatorException {
    if (this.services.putIfAbsent(i, svc) != null) {
      throw new ServiceLocatorException(Messages.SVCLOC_001, i);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <I> I locate(Class<I> svcIntf) throws ServiceLocatorException {
    return (I) services.get(svcIntf);
  }

}
