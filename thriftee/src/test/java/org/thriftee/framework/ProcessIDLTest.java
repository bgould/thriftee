package org.thriftee.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.thriftee.framework.ExportIDL;
import org.thriftee.framework.ProcessIDL;
import org.thriftee.framework.ThriftCommand;
import org.thriftee.framework.ThriftCommand.Generate;
import org.thriftee.framework.ThriftCommand.Generate.Flag;
import org.thriftee.util.FileUtil;

public class ProcessIDLTest {

	@Test
	public void testProcessIDL() throws IOException {
		File temp = ExportIDLTest.getTempDirForTest();
		File[] idlFiles = exportIDL(temp);
		ThriftCommand cmd = new ThriftCommand(Generate.PHP);
		cmd.addFlag(Flag.PHP_OOP);
		cmd.addFlag(Flag.PHP_NAMESPACE);
		ProcessIDL processor = new ProcessIDL();
		File zipFile = processor.process(idlFiles, temp, "php-library", cmd);
		
		FileInputStream fileIn = null;
		ZipInputStream zipIn = null;
		try {
			fileIn = new FileInputStream(zipFile);
			zipIn = new ZipInputStream(fileIn);
			for (ZipEntry entry; (entry = zipIn.getNextEntry()) != null; ) {
				System.out.println("Entry: " + entry.getName());
			}
		} finally {
			FileUtil.forceClosed(zipIn);
			FileUtil.forceClosed(fileIn);
		}
	}
	
	private File[] exportIDL(File temp) {
		try {
			ExportIDL exporter = new ExportIDL();
			return exporter.export(temp, ExportIDLTest.TEST_CLASSES);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
