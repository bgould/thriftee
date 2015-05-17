package org.thriftee.examples.usergroup.domain;

import java.io.Serializable;
import java.util.List;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class Group implements Serializable {

  private static final long serialVersionUID = 1L;

	private String name;

	private List<User> members;

	@ThriftField(1)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ThriftField(2)
	public List<User> getMembers() {
		return members;
	}

	public void setMembers(List<User> members) {
		this.members = members;
	}
	
}
