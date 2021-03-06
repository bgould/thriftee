/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.core.tests;

import org.thriftee.core.DefaultServiceLocator;
import org.thriftee.core.ServiceLocatorException;
import org.thriftee.examples.usergroup.service.GroupService;
import org.thriftee.examples.usergroup.service.GroupServiceImpl;
import org.thriftee.examples.usergroup.service.UserGroupException;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.examples.usergroup.service.UserServiceImpl;

import everything.Universe;
import everything.UniverseImpl;

public class TestServiceLocator extends DefaultServiceLocator {

  public TestServiceLocator() throws ServiceLocatorException {
    try {
      final UniverseImpl universe = new UniverseImpl();
      final UserServiceImpl userSvc = new UserServiceImpl();
      final GroupServiceImpl groupSvc = new GroupServiceImpl(userSvc);
      register(Universe.Iface.class, universe);
      register(UserService.Iface.class, userSvc);
      register(GroupService.Iface.class, groupSvc);
    } catch (UserGroupException e) {
      throw new ServiceLocatorException(
          e, ServiceLocatorException.Messages.SVCLOC_000);
    }
  }

}
