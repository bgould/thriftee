package org.thriftee.framework;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.scannotation.AnnotationDB;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.service.ThriftService;

public class Thrift {
		
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public ThriftCodecManager codecManager() {
		return thriftCodecManager;
	}
	
	public Set<Class<?>> structs() {
		return thriftStructs;
	}
	
	public Set<Class<?>> services() {
		return thriftServices;
	}
	
	public File thriftLibDir() {
		return this.thriftLibDir;
	}
	
	public File thriftExecutable() {
		return this.thriftExecutable;
	}
	
	public File tempDir() {
		return this.tempDir;
	}
	
	public File idlDir() {
		return this.idlDir;
	}
	
	public File[] idlFiles() {
		File[] newArr = new File[idlFiles == null ? 0 : idlFiles.length];
		if (newArr.length > 0) {
			System.arraycopy(idlFiles, 0, newArr, 0, idlFiles.length);
		}
		return newArr;
	}
	
	private final File tempDir;
	
	private final File idlDir;
	
	private final File thriftExecutable;
	
	private final File thriftLibDir;
	
	private final ThriftCodecManager thriftCodecManager;
	
	private final Set<Class<?>> thriftStructs;
	
	private final Set<Class<?>> thriftServices;
	
	private final Set<Class<?>> thriftEnums;
	
	private final File[] idlFiles;
	
	public Thrift(ThriftConfig config) {
		
		this.tempDir = config.tempDir();
		this.idlDir = new File(tempDir, "idl");
		
		final AnnotationDB annotations = new AnnotationDB();
		annotations.setScanClassAnnotations(true);
		annotations.setScanFieldAnnotations(false);
		annotations.setScanMethodAnnotations(false);
		annotations.setScanParameterAnnotations(false);
		try {
			config.scannotationConfigurator().configure(annotations);
			thriftServices = searchFor(ThriftService.class, annotations);
			thriftStructs = searchFor(ThriftStruct.class, annotations);
			thriftEnums = searchFor(ThriftEnum.class, annotations);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		thriftCodecManager = new ThriftCodecManager();
		
		logger.info("[Thrift] Initializing Thrift Services ----");
		logger.info("[Thrift] Services detected: " + thriftServices);
		logger.info("[Thrift] Structs detected: " + thriftStructs);
		logger.info("[Thrift] Enums detected: " + thriftEnums);
    	
		if (	config.thriftLibDir() != null	&& 
				config.thriftLibDir().exists()	&&
				new File(config.thriftLibDir(), "php/lib/Thrift").exists()	) {
			this.thriftLibDir = config.thriftLibDir();
		} else {
			this.thriftLibDir = null;
		}
		logger.info("[Thrift] Thrift library dir: " + thriftLibDir);
		
		if (	config.thriftExecutable() != null	&& 
				config.thriftExecutable().exists()	&&
				config.thriftExecutable().canExecute()	) {
			this.thriftExecutable = config.thriftExecutable();
		} else {
			this.thriftExecutable = null;
		}
		logger.info("[Thrift] Thrift executable: " + thriftExecutable);
		
		Set<Class<?>> allClasses = new HashSet<Class<?>>();
		allClasses.addAll(thriftServices);
		allClasses.addAll(thriftStructs);
		allClasses.addAll(thriftEnums);
				
    	logger.info("[Thrift] Exporting IDL files from Swift definitions");
		try {
			ExportIDL exporter = new ExportIDL();
			this.idlFiles = exporter.export(idlDir, allClasses);
		} catch (IOException e) {
			throw new ThriftStartupException("[Thrift] Problem writing IDL: " + e.getMessage(), e);
		}
		
		logger.info("[Thrift] Thrift initialization completed");

	}
	
	public static Set<Class<?>> searchFor(Class<? extends Annotation> annotation, AnnotationDB annotations) {
		final Set<String> names = annotations.getAnnotationIndex().get(annotation.getName());
		final Set<Class<?>> result = New.set();
			if (names != null) {
			for (String name : names) {
				try {
					final Class<?> clazz = Class.forName(name);
					result.add(clazz);
				} catch (ClassNotFoundException e) {
					System.err.println(
						"warning: discovered @" + annotation.getSimpleName() + " class via " +
						"classpath scanning, but could not load: " + name
					);
				}
			}
		}
		return result;
	}
	
}
