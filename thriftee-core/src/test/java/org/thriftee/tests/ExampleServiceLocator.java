package org.thriftee.tests;

import org.thriftee.examples.classicmodels.services.OrderService;
import org.thriftee.examples.classicmodels.services.OrderSessionBean;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.GroupServiceImpl;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.examples.usergroup.service.UserServiceImpl;
import org.thriftee.framework.DefaultServiceLocator;
import org.thriftee.framework.ServiceLocatorException;
import org.thriftee.thrift.protocol.Everything;
import org.thriftee.thrift.protocol.Universe;

public class ExampleServiceLocator extends DefaultServiceLocator {

  public ExampleServiceLocator() throws ServiceLocatorException {
    UserService userSvc = new UserServiceImpl();
    GroupService groupSvc = new GroupServiceImpl(userSvc);
    OrderService orderSvc = new OrderSessionBean();
    Universe universe = new Universe() {
      @Override
      public int grok(Everything everything) {
        return 42;
      }
    };
    register(UserService.class, userSvc);
    register(GroupService.class, groupSvc);
    register(OrderService.class, orderSvc);
    register(Universe.class, universe);
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
    if (Universe.class.equals(svcIntf)) {
      return (I) new Universe() {
        @Override
        public int grok(Everything everything) {
          return 42;
        }
      };
    }
    return null;
  }

}
