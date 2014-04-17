package org.thriftee.exceptions;

public class BaseSystemException extends RuntimeException {

	private static final long serialVersionUID = -5773493440474339812L;

	public BaseSystemException(String message) {
		super(message);
	}

	public BaseSystemException(Throwable cause) {
		super(cause);
	}

	public BaseSystemException(String message, Throwable cause) {
		super(message, cause);
	}

}
