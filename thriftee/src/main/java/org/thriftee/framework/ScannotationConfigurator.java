package org.thriftee.framework;

import java.io.IOException;

import org.scannotation.AnnotationDB;

public interface ScannotationConfigurator {

	public void configure(AnnotationDB db) throws IOException;
	
}
