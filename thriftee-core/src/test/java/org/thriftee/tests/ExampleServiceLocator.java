package org.thriftee.tests;

import org.thriftee.examples.presidents.PresidentService;
import org.thriftee.examples.presidents.PresidentServiceBean;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.GroupServiceImpl;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.examples.usergroup.service.UserServiceImpl;
import org.thriftee.framework.ServiceLocator;
import org.thriftee.framework.ServiceLocatorException;
import org.thriftee.framework.ThriftEE;

public class ExampleServiceLocator implements ServiceLocator {

  public ExampleServiceLocator() {
  }

  @Override
  @SuppressWarnings("unchecked")
  public <I> I locate(ThriftEE thriftee, Class<I> svcIntf)
      throws ServiceLocatorException {
    if (UserService.class.equals(svcIntf)) {
      return (I) new UserServiceImpl();
    }
    if (GroupService.class.equals(svcIntf)) {
      return (I) new GroupServiceImpl(new UserServiceImpl());
    }
    if (PresidentService.class.equals(svcIntf)) {
      return (I) new PresidentServiceBean();
    }
    return null;
  }

}
