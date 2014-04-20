package org.thriftee.exceptions;

public class ThriftRuntimeException extends RuntimeException implements ThriftException {

	private static final long serialVersionUID = 2433714939544501889L;

	private final ThriftMessage _thrifteeMessage;
	
	private final Object[] _arguments;
	
	@Override
	public ThriftMessage getThrifteeMessage() {
		return _thrifteeMessage;
	}
	
	@Override
	public Object[] getArguments() {
		return _arguments;
	}

	public ThriftRuntimeException(final ThriftMessage thrifteeMessage, final Object... arguments) {
		super(ThriftMessageSupport.getMessage(thrifteeMessage, arguments));
		this._thrifteeMessage = thrifteeMessage;
		this._arguments = arguments;
	}

	public ThriftRuntimeException(final Throwable cause, final ThriftMessage thrifteeMessage, final Object... arguments) {
		super(ThriftMessageSupport.getMessage(thrifteeMessage, arguments), cause);
		this._thrifteeMessage = thrifteeMessage;
		this._arguments = arguments;
	}	
	
}
