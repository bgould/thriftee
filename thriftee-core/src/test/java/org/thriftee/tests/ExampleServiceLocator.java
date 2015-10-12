package org.thriftee.tests;

import org.thriftee.examples.classicmodels.services.OrderService;
import org.thriftee.examples.classicmodels.services.OrderSessionBean;
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
    OrderService orderSvc = new OrderSessionBean();
    register(UserService.class, userSvc);
    register(GroupService.class, groupSvc);
    register(OrderService.class, orderSvc);
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
    if (OrderService.class.equals(svcIntf)) {
      return (I) new OrderSessionBean();
    }
    return null;
  }

}
