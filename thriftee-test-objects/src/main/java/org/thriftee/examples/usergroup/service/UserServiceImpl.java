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

import java.util.HashMap;
import java.util.Map;

import org.thriftee.examples.usergroup.domain.User;
import org.thriftee.examples.usergroup.service.UserService;
import org.thriftee.examples.usergroup.service.UserGroupException;

public class UserServiceImpl implements UserService.Iface {

  private final Map<String, User> testUsers = new HashMap<String, User>();
  {
    String[][] users = {
      {"Alan", "Aardvark", "alanaardvark@example.com", "Aardvark, Alan", "aaardvark", },
      {"Bonnie", "Bat", "bonniebat@example.com", "Bat, Bonnie", "bbat", },
      {"Chris", "Crustacean", "chriscrustacean@example.com", "Crustacean, Chris", "ccrustacean", },
      {"Daphne", "Dingo", "daphnedingo@example.com", "Dingo, Daphne", "ddingo", },
      {"Elmer", "Eagle", "elmereagle@example.com", "Eagle, Elmer", "eeagle", },
      {"Florence", "Fox", "florencefox@example.com", "Fox, Florence", "ffox", },
      {"Gary", "Grasshopper", "garygrasshopper@example.com", "Grasshopper, Gary", "ggrasshopper", },
      {"Helga", "Hamster", "helgahamster@example.com", "Hamster, Helga", "hhamster", },
    };
    for (String[] userData : users) {
      User user = new User();
      user.setFirstName(userData[0]);
      user.setLastName(userData[1]);
      user.setEmail(userData[2]);
      user.setDisplayName(userData[3]);
      user.setUid(userData[4]);
      testUsers.put(user.getUid(), user);
    }
  }

  @Override
  public User find(String uid) throws UserGroupException {
    return testUsers.get(uid);
  }
  
}
