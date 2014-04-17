package org.thriftee.framework;

/**
 * 
 * @author bcg
 */
public class ThriftEEFactory {
	
	public ThriftEE create(ThriftEEConfig config) throws ThriftEEStartupException {
		return new ThriftEE(config);
	}
	
}
