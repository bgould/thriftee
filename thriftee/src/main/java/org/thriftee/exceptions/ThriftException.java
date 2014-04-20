package org.thriftee.exceptions;

public interface ThriftException {

	public ThriftMessage getThrifteeMessage();
	
	public Object[] getArguments();
	
}
