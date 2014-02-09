package org.thriftee.framework;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.scannotation.AnnotationDB;
import org.scannotation.WarUrlFinder;

public class WarFileScannotationConfigurator implements ScannotationConfigurator {

	private final ServletContext ctx;
	
	public WarFileScannotationConfigurator(ServletContext ctx) {
		this.ctx = ctx;
	}
	
	@Override
	public void configure(AnnotationDB db) throws IOException {
		db.scanArchives(WarUrlFinder.findWebInfLibClasspaths(ctx));
		db.scanArchives(WarUrlFinder.findWebInfClassesPath(ctx));
	}
	
}
