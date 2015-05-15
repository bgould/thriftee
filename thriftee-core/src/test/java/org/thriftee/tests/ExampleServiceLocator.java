package org.thriftee.tests;

import org.thriftee.examples.presidents.PresidentService;
import org.thriftee.examples.presidents.PresidentServiceBean;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.GroupServiceImpl;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.examples.usergroup.service.UserServiceImpl;
import org.thriftee.framework.DefaultServiceLocator;
import org.thriftee.framework.ServiceLocatorException;

public class ExampleServiceLocator extends DefaultServiceLocator {

  public ExampleServiceLocator() throws ServiceLocatorException {
    UserService userSvc = new UserServiceImpl();
    GroupService groupSvc = new GroupServiceImpl(userSvc);
    PresidentService pres = new PresidentServiceBean();
    register(UserService.class, userSvc);
    register(GroupService.class, groupSvc);
    register(PresidentService.class, pres);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <I> I locate(final Class<I> svcIntf)
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
