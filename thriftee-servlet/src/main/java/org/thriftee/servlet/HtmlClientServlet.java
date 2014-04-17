package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.WebServlet;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.framework.ThriftEEStartupException;

@WebServlet("/clients/html/*")
public class HtmlClientServlet extends ZipFileBrowsingServlet {

	private static final long serialVersionUID = -3518542031465043696L;

	private File htmlClientLibrary;
	
	public void init() {
		logger.info("[HtmlClientServlet] Generating HTML client library");
		try {
			ThriftCommand cmd = new ThriftCommand(Generate.HTML);
			cmd.setRecurse(true);
			//cmd.addFlag(Flag.HTML_STANDALONE);
			htmlClientLibrary = new ProcessIDL().process(
				//thrift().idlFiles(), thrift().tempDir(), "html-client", cmd
				new File[] { new File(thrift().idlDir(), "thrift/global.thrift") }, 
				thrift().tempDir(),
				"html-client",
				cmd
			);
			logger.info(
				"[HtmlClientServlet] HTML client library created at : " + 
				htmlClientLibrary.getAbsolutePath()
			);
		} catch (IOException e) {
			throw new ThriftEEStartupException(
				"[HtmlClientServlet] Problem generating HTML library: " + e.getMessage()
			);
		}
	}
	
	@Override
	protected File zipFile() {
		return htmlClientLibrary;
	}
}
