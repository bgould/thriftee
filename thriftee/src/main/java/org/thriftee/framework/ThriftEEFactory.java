package org.thriftee.framework;

/**
 * 
 * @author bcg
 */
public class ThriftEEFactory {
	
	public ThriftEE create(ThriftEEConfig config) throws ThriftStartupException {
		return new ThriftEE(config);
	}
	
}
