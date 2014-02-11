package org.thriftee.framework;


public class ThriftFactory {
	
	public Thrift create(ThriftConfig config) throws ThriftStartupException {
		return new Thrift(config);
	}
	
}
