package org.thriftee.examples.usergroup.domain;

import java.io.Serializable;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class User implements Serializable {

  private static final long serialVersionUID = 1L;

	private String uid;

	private String firstName;

	private String lastName;

	private String displayName;

	private String email;

	@ThriftField(1)
	public String getUid() {
		return uid;
	}

	@ThriftField
	public void setUid(String uid) {
		this.uid = uid;
	}

	@ThriftField(2)
	public String getFirstName() {
		return firstName;
	}

	@ThriftField
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@ThriftField(3)
	public String getLastName() {
		return lastName;
	}

	@ThriftField
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@ThriftField(4)
	public String getDisplayName() {
		return displayName;
	}

	@ThriftField
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@ThriftField(5)
	public String getEmail() {
		return email;
	}

	@ThriftField
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "User [uid=" + uid + ", firstName=" + firstName + ", lastName="
				+ lastName + ", displayName=" + displayName + ", email="
				+ email + "]";
	}

}
