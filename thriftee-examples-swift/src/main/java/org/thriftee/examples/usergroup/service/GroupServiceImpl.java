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
package org.thriftee.examples.usergroup.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.thriftee.examples.usergroup.domain.Group;
import org.thriftee.examples.usergroup.domain.User;

@Stateless(name="GroupServiceBean")
@Remote(GroupService.class)
public class GroupServiceImpl implements GroupService {

  private final Map<String, Group> testGroups = new HashMap<>();

  @EJB
  private UserService userService;

  public GroupServiceImpl(UserService userService) {
    this.userService = userService;
    init();
  }

  public GroupServiceImpl() {
  }

  @PostConstruct
  public void init() {
    final String[][] groups = {
      { "Mammals", "aaardvark", "bbat", "ddolphin", "ffox", "hhamster", },
      { "Winged", "eeagle", "ggrasshopper", "bbat", },
      { "Marine", "ddolphin", "ccrustacean", },
      { "Insects", "ggrasshopper", },
      { "Birds", "eeagle", }
    };
    for (String[] groupData : groups) {
      final Group group = new Group();
      final List<User> users = new ArrayList<User>();
      group.setName(groupData[0]);
      for (int i = 1; i < groupData.length; i++) {
        try {
          final User user = this.userService.find(groupData[i]);
          if (user != null) {
            users.add(user);
          }
        } catch (UserGroupException e) {
          throw new RuntimeException(e);
        }
      }
      group.setMembers(users);
      testGroups.put(group.getName(), group);
    }
  }

  @Override
  public Group find(String name) throws UserGroupException {
    final Group result = testGroups.get(name);
    return result;
  }

  @Override
  public boolean addUserToGroup(Group group, User user) throws UserGroupException {
    Group _group = testGroups.get(group.getName());
    User _user = userService.find(user.getUid());
    if (_group != null && _user != null) {
      final List<User> members = _group.getMembers();
      if (members != null) {
        for (User member : members) {
          if (member.getUid().equals(_user.getUid())) {
            return true;
          }
        }
      } else {
        _group.setMembers(new ArrayList<User>());
      }
      _group.getMembers().add(_user);
      return true;
    }
    return false;
  }

  @Override
  public List<String> groupNames(final User user) throws UserGroupException {
    final List<String> groupNames = new ArrayList<String>();
    for (Group group : testGroups.values()) {
      final List<User> members = group.getMembers();
      for (User _user : members) {
        if (user.getUid().equals(_user.getUid())) {
          groupNames.add(group.getName());
        }
      }
    }
    return groupNames;
  }

}
