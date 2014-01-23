package org.thriftee.framework;

import java.io.File;
import java.io.Serializable;

public class ThriftConfig implements Serializable {

	private static final long serialVersionUID = 8148668461656853500L;

	private File tempDir;
	
	private File thriftExecutable;
	
	private File thriftLibDir;
	
	private ThriftConfig(File tempDir, File thriftExecutable, File thriftLibDir) {
		super();
		this.tempDir = tempDir;
		this.thriftExecutable = thriftExecutable;
		this.thriftLibDir = thriftLibDir;
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
	
	public static class Builder {
		
		private File tempDir;
		
		private File thriftExecutable;
		
		private File thriftLibDir;
		
		public Builder() {
		}
		
		public Builder tempDir(File tempDir) {
			if (tempDir == null) {
				throw new IllegalArgumentException("tempDir cannot be null");
			}
			this.tempDir = tempDir;
			return this;
		}
		
		public Builder thriftExecutable(File file) {
			this.thriftExecutable = file;
			return this;
		}
		
		public Builder thriftLibDir(File file) {
			this.thriftLibDir = file;
			return this;
		}
		
		public ThriftConfig build() {
			if (tempDir == null) {
				throw new IllegalArgumentException("tempDir cannot be null");
			}
			return new ThriftConfig(tempDir, thriftExecutable, thriftLibDir);
		}
		
	}
	
}
