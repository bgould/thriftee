package org.thriftee.framework;

public class ThriftStartupException extends RuntimeException {

	private static final long serialVersionUID = 4082854407680864687L;

	public ThriftStartupException(String message, Throwable cause) {
		super(message, cause);
	}

	public ThriftStartupException(String message) {
		super(message);
	}

	public ThriftStartupException(Throwable cause) {
		super(cause);
	}
	
}
