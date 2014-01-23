package test1.service;


import test1.domain.User;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface UserService {

	@ThriftMethod
	public User find(String uid) throws UserGroupException;
	
}
