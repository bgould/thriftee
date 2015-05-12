package org.thriftee.examples.usergroup.service;


import org.thriftee.examples.usergroup.domain.User;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface UserService {

  @ThriftMethod
  public User find(@ThriftField(name="uid") String uid) throws UserGroupException;

}
