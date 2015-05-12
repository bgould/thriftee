package org.thriftee.servlet;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.thriftee.framework.ServiceLocator;
import org.thriftee.framework.ServiceLocatorException;
import org.thriftee.framework.ServiceLocatorException.ServiceLocatorMessage;
import org.thriftee.framework.ThriftEE;

import com.facebook.swift.service.ThriftService;

public class DefaultEJBServiceLocator implements ServiceLocator {

  @Override
  public <I> I locate(ThriftEE thriftee, Class<I> svcIntf) throws ServiceLocatorException {
    final InitialContext ic;
    try {
      ic = new InitialContext();
    } catch (NamingException e) {
      throw new ServiceLocatorException(e, ServiceLocatorMessage.SVCLOC_000);
    }
    // TODO: make this more intelligent
    final ThriftService annotation = svcIntf.getAnnotation(ThriftService.class);
    if (annotation == null) {
      throw new IllegalArgumentException("Service interface not annotated.");
    }
    final String name = annotation.value();
    final String svcName = (StringUtils.trimToNull(name) == null) ? svcIntf.getSimpleName() : name;
    final String jndiName = "java:app/" + svcName + "Bean";
    final I result;
    try {
      @SuppressWarnings("unchecked")
      final I bean = (I) ic.lookup(jndiName);
      result = bean;
    } catch (NamingException e) {
      throw new ServiceLocatorException(e, ServiceLocatorMessage.SVCLOC_000);
    }
    return result;
  }

}
