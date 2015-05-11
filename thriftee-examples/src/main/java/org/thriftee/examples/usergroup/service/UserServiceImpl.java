package org.thriftee.examples.usergroup.service;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.thriftee.examples.usergroup.domain.User;

@Stateless
@Remote(UserService.class)
public class UserServiceImpl implements UserService {

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
