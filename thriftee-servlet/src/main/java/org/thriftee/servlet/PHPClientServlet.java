package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

@WebServlet("/clients/php/*")
public class PHPClientServlet extends ZipFileBrowsingServlet {

	private static final long serialVersionUID = -3518542031465043696L;

	private File phpClientLibrary;
	
	@Override
	public void init() throws ServletException {
		logger.info("[PHPClientServlet] Generating PHP client library");
		try {
			final File[] extraDirs;
			if (thrift().thriftLibDir() != null) {
				File phpLib = new File(thrift().thriftLibDir(), "php/lib");
				extraDirs = new File[] { phpLib };
			} else {
				extraDirs = new File[0];
			}
			ThriftCommand cmd = new ThriftCommand(Generate.PHP);
			cmd.addFlag(Flag.PHP_NAMESPACE);
			cmd.addFlag(Flag.PHP_OOP);
			if (thrift().thriftExecutable() != null) {
				cmd.setThriftCommand(thrift().thriftExecutable().getAbsolutePath());
			}
			phpClientLibrary = new ProcessIDL().process(
				thrift().idlFiles(), 
				thrift().tempDir(), 
				"php-client", 
				cmd,
				extraDirs
			);
			logger.info(
				"[JQueryClientServlet] PHP client library created at : {}", 
				phpClientLibrary.getAbsolutePath()
			);
		} catch (IOException e) {
			throw new ServletException(
				"[JQueryClientServlet] Problem generating PHP library: " + 
				e.getMessage(), e
			);
		}
	}
	
	@Override
	protected File zipFile() {
		return phpClientLibrary;
	}
	
}
