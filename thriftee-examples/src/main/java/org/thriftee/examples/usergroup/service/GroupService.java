package org.thriftee.examples.usergroup.service;

import java.util.List;

import org.thriftee.examples.usergroup.domain.Group;
import org.thriftee.examples.usergroup.domain.User;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface GroupService {

	@ThriftMethod
	public Group find(String name) throws UserGroupException;
	
	@ThriftMethod
	public boolean addUserToGroup(Group group, User user) throws UserGroupException;
	
	@ThriftMethod
	public List<String> groupNames(User user) throws UserGroupException;
	
}
