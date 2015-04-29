package org.thriftee.framework;

import java.io.File;
import java.io.Serializable;

public class ThriftEEConfig implements Serializable {

	private static final long serialVersionUID = 8148668461656853500L;

	private final File tempDir;
	
	private final File thriftExecutable;
	
	private final File thriftLibDir;
	
	private final ScannotationConfigurator scannotationConfigurator;
	
	private ThriftEEConfig(
			final File tempDir, 
			final File thriftExecutable, 
			final File thriftLibDir, 
			final ScannotationConfigurator configurator) {
		super();
		this.tempDir = tempDir;
		this.thriftExecutable = thriftExecutable;
		this.thriftLibDir = thriftLibDir;
		this.scannotationConfigurator = configurator;
	}

	public File tempDir() {
		return this.tempDir;
	}
	
	public File thriftExecutable() {
		return this.thriftExecutable;
	}
	
	public File thriftLibDir() {
		return this.thriftLibDir;
	}
	
	public ScannotationConfigurator scannotationConfigurator() {
		return this.scannotationConfigurator;
	}
	
	public static class Factory {
		
		private File tempDir;
		
		private File thriftExecutable;
		
		private File thriftLibDir;
		
		private ScannotationConfigurator scannotationConfigurator;
		
		public void setTempDir(File tempDir) {
//			if (tempDir == null) {
//				throw new IllegalArgumentException("tempDir cannot be null");
//			}
			this.tempDir = tempDir;
		}
		
		public void setThriftExecutable(File file) {
			this.thriftExecutable = file;
		}
		
		public void setThriftLibDir(File file) {
			this.thriftLibDir = file;
		}
		
		public void setScannotationConfigurator(ScannotationConfigurator configurator) {
			this.scannotationConfigurator = configurator;
		}

		public File getTempDir() {
			return tempDir;
		}

		public File getThriftExecutable() {
			return thriftExecutable;
		}

		public File getThriftLibDir() {
			return thriftLibDir;
		}

		public ScannotationConfigurator getScannotationConfigurator() {
			return scannotationConfigurator;
		}
		
		public ThriftEEConfig newInstance() {
			if (tempDir == null) {
				throw new IllegalArgumentException("tempDir cannot be null");
			}
			if (scannotationConfigurator == null) {
				throw new IllegalArgumentException("scannotationConfigurator cannot be null");
			}
			return new ThriftEEConfig(tempDir, thriftExecutable, thriftLibDir, scannotationConfigurator);
		}
		
	}
	
	public static class Builder {
		
		private final Factory factory = new Factory();
		
		public Builder() {
		}
		
		public Builder tempDir(final File tempDir) {
			if (tempDir == null) {
				throw new IllegalArgumentException("tempDir cannot be null");
			}
			factory.setTempDir(tempDir);
			return this;
		}
		
		public Builder thriftExecutable(final File file) {
			factory.setThriftExecutable(file);
			return this;
		}
		
		public Builder thriftLibDir(final File file) {
			factory.setThriftLibDir(file);
			return this;
		}
		
		public Builder scannotationConfigurator(final ScannotationConfigurator configurator) {
			factory.setScannotationConfigurator(configurator);
			return this;
		}
		
		public ThriftEEConfig build() {
			return factory.newInstance();
		}
		
	}
	
}
