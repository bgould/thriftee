package org.thriftee.exceptions;

import java.io.Serializable;

public interface ThriftMessage extends Serializable {

	public String getCode();
	
	public String getMessage();
	
}
