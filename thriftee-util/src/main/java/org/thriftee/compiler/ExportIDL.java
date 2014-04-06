package org.thriftee.compiler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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
			Pattern namespacePattern = Pattern.compile("namespace java\\.swift (.+)");
			String thriftFileStr = swiftFileStr;
			Matcher m = namespacePattern.matcher(swiftFileStr);
			if (m.find()) {
				String namespace = m.group(1);
				String php_namespace = "\\" + namespace.replace('.', '\\');
				StringBuilder sb = new StringBuilder();
				sb.append("namespace cpp  ").append(namespace).append('\n');
				sb.append("namespace d    ").append(namespace).append('\n');
				sb.append("namespace java ").append(namespace).append('\n');
				sb.append("namespace php  ").append(namespace).append('\n');
				sb.append("namespace perl ").append(namespace).append('\n');
//				sb.append("namespace php_namespace ").append(php_namespace).append('\n');
				thriftFileStr = m.replaceFirst(sb.toString());
				System.err.println("Rewriting swift namespace: " + sb);
			} else {
				System.err.println("Could not find swift namespace in file: \n---------------------------------------\n" + swiftFileStr);
			}
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
