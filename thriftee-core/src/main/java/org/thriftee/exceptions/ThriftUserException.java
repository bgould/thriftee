package org.thriftee.exceptions;

public class ThriftUserException extends ThriftExceptionBase {

	private static final long serialVersionUID = 7045073312868483749L;

	public ThriftUserException(ThriftMessage thrifteeMessage, Object... arguments) {
		super(thrifteeMessage, arguments);
	}

	public ThriftUserException(Throwable cause, ThriftMessage thrifteeMessage, Object... arguments) {
		super(cause, thrifteeMessage, arguments);
	}

}
