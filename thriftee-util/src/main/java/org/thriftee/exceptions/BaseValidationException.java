package org.thriftee.exceptions;

public class BaseValidationException extends Exception {

	private static final long serialVersionUID = -334559959372690054L;

	public BaseValidationException(String message) {
		super(message);
	}

	public BaseValidationException(Throwable cause) {
		super(cause);
	}

	public BaseValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
