package org.thriftee.exceptions;

public final class ThriftMessageSupport {

	private ThriftMessageSupport() {
	}
	
	public static String getMessage(ThriftMessage message, Object[] arguments) {
		return String.format(message.getMessage(), arguments);
	}
	
}
