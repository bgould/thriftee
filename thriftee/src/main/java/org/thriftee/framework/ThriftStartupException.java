package org.thriftee.framework;

import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftSystemException;

public class ThriftStartupException extends ThriftSystemException {

	private static final long serialVersionUID = 4082854407680864687L;

	public ThriftStartupException(ThriftMessage thrifteeMessage, Object... arguments) {
		super(thrifteeMessage, arguments);
	}

	public ThriftStartupException(Throwable cause, ThriftMessage thrifteeMessage, Object... arguments) {
		super(cause, thrifteeMessage, arguments);
	}
	
	public static enum ThriftStartupMessage implements ThriftMessage {
		
		STARTUP_001("A problem occurred writing the IDL for the annotated classes: %s"),
		STARTUP_002("A problem occurred scanning the Swift annotations at startup: %s"),
		STARTUP_003("A problem occurred parsing generated IDL at startup: %s"),
		STARTUP_004("Generated IDL did not contain a global.thrift file"),
		;

		private final String _message;
		
		private ThriftStartupMessage(String message) {
			this._message = message;
		}
		
		@Override
		public String getCode() {
			return name();
		}

		@Override
		public String getMessage() {
			return _message;
		}
		
	}
	
}
