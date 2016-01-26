namespace cpp  org.thriftee.examples.usergroup.service
namespace d    org.thriftee.examples.usergroup.service
namespace java org.thriftee.examples.usergroup.service
namespace php  org.thriftee.examples.usergroup.service
namespace perl org.thriftee.examples.usergroup.service


include "org_thriftee_examples_usergroup_domain.thrift"


exception UserGroupException {
  1:  string message;
}

service UserService {
  org_thriftee_examples_usergroup_domain.User find(1:  string uid) throws (1: UserGroupException ex1);
}

service GroupService {
  bool addUserToGroup(1:  org_thriftee_examples_usergroup_domain.Group arg0, 2:  org_thriftee_examples_usergroup_domain.User arg1) throws (1: UserGroupException ex1);
  org_thriftee_examples_usergroup_domain.Group find(1:  string arg0) throws (1: UserGroupException ex1);
  list<string> groupNames(1:  org_thriftee_examples_usergroup_domain.User arg0) throws (1: UserGroupException ex1);
}
