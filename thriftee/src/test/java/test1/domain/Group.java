package test1.domain;

import java.util.List;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Group {

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
