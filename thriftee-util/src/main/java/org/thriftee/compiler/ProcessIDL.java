package org.thriftee.compiler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.thriftee.util.FileUtil;

public class ProcessIDL {

	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public ProcessIDL() {
	}
	
	public File process(File[] idlFiles, File workDir, String zipName, ThriftCommand cmd, File... extraZipDirectories) throws IOException {
		File outputDir = getOutputDir(workDir, zipName);
		if (outputDir.exists()) {
			FileUtil.deleteRecursively(outputDir);
		}
		if (!outputDir.mkdirs()) {
			throw new IOException("could not create output directory: " + outputDir.getAbsolutePath());
		}
		cmd.setOutputLocation(outputDir);
		for (File file : idlFiles) {
			cmd.setThriftFile(file);
			logger.info("generating PHP library from thrift IDL: " + cmd.commandString());
			ProcessBuilder pb = new ProcessBuilder(cmd.command());
			pb.inheritIO();
			Process process = pb.start();
			try {
				int exit = process.waitFor();
				if (exit > 0) {
					throw new IOException("thrift generation failed with exit code: " + exit);
				}
			} catch (InterruptedException e) {
				throw new IOException("Thrift generation process was interrupted.", e);
			}
		}
		File zipFile = new File(workDir, zipName + ".zip");
		if (zipFile.exists()) {
			if (!zipFile.delete()) {
				throw new IOException("Could not delete existing zip file: " + zipFile.getAbsolutePath());
			}
		}
		FileUtil.createZipFromDirectory(zipFile, "", outputDir, extraZipDirectories);
		return zipFile;
	}
	
	public File getOutputDir(File workDir, String zipName) {
		File outputDir = new File(workDir, zipName);
		return outputDir;
	}
	
}
