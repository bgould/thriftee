package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.framework.ThriftEEStartupException;

@WebServlet("/clients/jquery/*")
public class JQueryClientServlet extends ZipFileBrowsingServlet {

	private static final long serialVersionUID = -3518542031465043696L;

	private File jqueryClientLibrary;
	
	public void init() {
		logger.info("[JQueryClientServlet] Generating jQuery client library");
		try {
			ThriftCommand cmd = new ThriftCommand(Generate.JS);
			cmd.addFlag(Flag.JS_JQUERY);
			jqueryClientLibrary = new ProcessIDL().process(
				thrift().idlFiles(), thrift().tempDir(), "jquery-client", cmd
			);
			logger.info(
				"[JQueryClientServlet] jQuery client library created at : " + 
				jqueryClientLibrary.getAbsolutePath()
			);
		} catch (IOException e) {
			throw new ThriftEEStartupException(
				"[JQueryClientServlet] Problem generating jQuery library: " + e.getMessage()
			);
		}
	}
	
	@Override
	protected File zipFile() {
		return jqueryClientLibrary;
	}
	
}
