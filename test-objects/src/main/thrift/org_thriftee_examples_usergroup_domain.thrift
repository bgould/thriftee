namespace cpp  org.thriftee.examples.usergroup.domain
namespace d    org.thriftee.examples.usergroup.domain
namespace java org.thriftee.examples.usergroup.domain
namespace php  org.thriftee.examples.usergroup.domain
namespace perl org.thriftee.examples.usergroup.domain




struct User {
  1:  string uid;
  2:  string firstName;
  3:  string lastName;
  4:  string displayName;
  5:  string email;
}

struct Group {
  1:  string name;
  2:  list<User> members;
}

