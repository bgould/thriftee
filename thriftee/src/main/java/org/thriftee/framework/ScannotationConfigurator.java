package org.thriftee.framework;

import java.io.IOException;

import org.scannotation.AnnotationDB;

/**
 * Implementors of this class are responsible for configuring an 
 * {@link org.scannotation.AnnotationDB} in order to search for runtime
 * annotations.
 * @author y12784
 */
public interface ScannotationConfigurator {

	/**
	 * Configures and instance of {@link org.scannotation.AnnotationDB} with
	 * classpaths to search.
	 * @param db The database to configure
	 * @throws IOException
	 */
	public void configure(AnnotationDB db) throws IOException;
	
}
