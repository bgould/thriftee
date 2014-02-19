package org.thriftee.examples.usergroup.service;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class UserGroupException extends Exception {

	private static final long serialVersionUID = -5553095593272542240L;
	
	public UserGroupException(String message, Throwable cause) {
		super(message, cause);
	}

	@ThriftConstructor
	public UserGroupException(String message) {
		super(message);
	}

	@Override
	@ThriftField(1)
	public String getMessage() {
		return super.getMessage();
	}
	
	public static class UserGroupExceptionBuilder {
		
	}
	
}
