package org.thriftee.framework;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.thriftee.util.FileUtil;

import com.facebook.swift.generator.swift2thrift.Generator;

public class ExportIDL {

	protected final Logger logger = Logger.getLogger(getClass().getName()); 
	
	public ExportIDL() {
	}

	public File[] export(File basedir, Set<Class<?>> classes) throws IOException {		
		if (basedir.exists()) {
			FileUtil.deleteRecursively(basedir);
		}
		File swiftDir = new File(basedir, "swift");
		File thriftDir = new File(basedir, "thrift");
		if (!swiftDir.mkdirs() || !thriftDir.mkdirs()) {
			throw new IllegalArgumentException(
				"could not write necessary directories to : " + 
				basedir.getAbsolutePath()
			);
		}
		logger.info("[ExportIDL] Creating IDL in temporary directory: " + basedir.getAbsolutePath());
		logger.info("[ExportIDL] Generating IDL for classes: " + classes);
		Generator generator = new Generator();
		generator.setTempDir(swiftDir);
		generator.setClasses(classes.toArray(new Class[classes.size()]));
		generator.generate();
		File[] idlFiles = thriftFilesIn(swiftDir);
		for (File swiftFile : idlFiles) { // now copy to the 'swift' files to 'thrift'
			File thriftFile = new File(thriftDir, swiftFile.getName());
			String swiftFileStr = FileUtil.readAsString(swiftFile);
			// TODO: Hardcoded
			String thriftFileStr = swiftFileStr.replaceAll("java\\.swift com\\.fuame", "php Fuame");
			thriftFileStr = "namespace java com.fuame.services.client\n" + thriftFileStr;
			FileUtil.writeStringToFile(thriftFileStr, thriftFile);
			logger.info("[ExportIDL] Swift file: " + swiftFile.getAbsolutePath());
			logger.info("[ExportIDL]  Copied to: " + thriftFile.getAbsolutePath());
		}
		return thriftFilesIn(thriftDir);
	}
	
	protected File[] thriftFilesIn(File dir) {
		FileFilter filter = new FileFilter() {
			private final Pattern matcher = Pattern.compile(".*\\.thrift$");
			@Override
			public boolean accept(File pathname) {
				if (matcher.matcher(pathname.getName()).matches()) {
					return true;
				}
				return false;
			}
		};
		File[] thriftFiles = dir.listFiles(filter);
		return thriftFiles;
	}
	
}
