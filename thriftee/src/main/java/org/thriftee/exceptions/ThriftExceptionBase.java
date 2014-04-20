package org.thriftee.exceptions;

public class ThriftExceptionBase extends Exception implements ThriftException {

	private static final long serialVersionUID = 4753565699269577159L;

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

	public ThriftExceptionBase(final ThriftMessage thrifteeMessage, final Object... arguments) {
		super(ThriftMessageSupport.getMessage(thrifteeMessage, arguments));
		this._thrifteeMessage = thrifteeMessage;
		this._arguments = arguments;
	}

	public ThriftExceptionBase(final Throwable cause, final ThriftMessage thrifteeMessage, final Object... arguments) {
		super(ThriftMessageSupport.getMessage(thrifteeMessage, arguments), cause);
		this._thrifteeMessage = thrifteeMessage;
		this._arguments = arguments;
	}	
	
}
