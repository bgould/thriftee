package test1.service;

import java.util.List;

import test1.domain.Group;
import test1.domain.User;


import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface GroupService {

	@ThriftMethod
	public Group find(String name);
	
	@ThriftMethod
	public String addUserToGroup(Group group, User user);
	
	@ThriftMethod
	public List<String> groupNames(User user);
	
}
