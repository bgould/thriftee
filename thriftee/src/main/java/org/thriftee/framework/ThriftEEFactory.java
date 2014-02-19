package org.thriftee.framework;


public class ThriftEEFactory {
	
	public ThriftEE create(ThriftEEConfig config) throws ThriftEEStartupException {
		return new ThriftEE(config);
	}
	
}
