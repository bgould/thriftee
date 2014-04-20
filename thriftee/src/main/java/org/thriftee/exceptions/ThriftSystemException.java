package org.thriftee.exceptions;

public class ThriftSystemException extends ThriftExceptionBase {

	private static final long serialVersionUID = 2082197768718947206L;

	public ThriftSystemException(ThriftMessage thrifteeMessage, Object... arguments) {
		super(thrifteeMessage, arguments);
	}

	public ThriftSystemException(Throwable cause, ThriftMessage thrifteeMessage, Object... arguments) {
		super(cause, thrifteeMessage, arguments);
	}
}
