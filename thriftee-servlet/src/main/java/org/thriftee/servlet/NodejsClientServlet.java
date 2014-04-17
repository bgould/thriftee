package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.framework.ThriftEEStartupException;

@WebServlet("/clients/nodejs/*")
public class NodejsClientServlet extends ZipFileBrowsingServlet {

	private static final long serialVersionUID = -3518542031465043696L;

	private File clientLibrary;
	
	public void init() {
		logger.info("[NodejsClientServlet] Generating node.js client library");
		try {
			ThriftCommand cmd = new ThriftCommand(Generate.JS);
			cmd.addFlag(Flag.JS_NODE);
			clientLibrary = new ProcessIDL().process(
				thrift().idlFiles(), thrift().tempDir(), "nodejs-client", cmd
			);
			logger.info(
				"[NodejsClientServlet] node.js client library created at : " + 
				clientLibrary.getAbsolutePath()
			);
		} catch (IOException e) {
			throw new ThriftEEStartupException(
				"[NodejsClientServlet] Problem generating node.js library: " + e.getMessage()
			);
		}
	}
	
	@Override
	protected File zipFile() {
		return clientLibrary;
	}
	
}
