package org.thriftee.framework;

public class ThriftEEStartupException extends RuntimeException {

	private static final long serialVersionUID = 4082854407680864687L;

	public ThriftEEStartupException(String message, Throwable cause) {
		super(message, cause);
	}

	public ThriftEEStartupException(String message) {
		super(message);
	}

	public ThriftEEStartupException(Throwable cause) {
		super(cause);
	}
	
}
