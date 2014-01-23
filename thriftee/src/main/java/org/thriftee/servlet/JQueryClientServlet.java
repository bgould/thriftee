package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;

import org.thriftee.framework.ProcessIDL;
import org.thriftee.framework.ThriftCommand;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.framework.ThriftCommand.Generate;
import org.thriftee.framework.ThriftCommand.Generate.Flag;

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
			throw new ThriftStartupException(
				"[JQueryClientServlet] Problem generating jQuery library: " + e.getMessage()
			);
		}
	}
	
	@Override
	protected File zipFile() {
		return jqueryClientLibrary;
	}
	
}
