package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.framework.ThriftEEStartupException;

public abstract class ClientServlet extends ZipFileBrowsingServlet {

	private static final long serialVersionUID = -3518542031465043696L;
	
	private Map<String, File> zipFiles = new HashMap<String, File>();
	
	private Set<ThriftCommand.Generate> clientTypes = new LinkedHashSet<ThriftCommand.Generate>(); 
	
	public void init() {
		logger.info("[ClientServlet] Generating client libraries");
		try {
			final File tempDir = new File(thrift().tempDir(), "clients");
			final File global = new File(thrift().idlDir(), "thrift/global.thrift");
			for (Generate clientType : clientTypes) {
				final ThriftCommand cmd = new ThriftCommand(clientType);
				cmd.setRecurse(true);
				final File clientZip = new ProcessIDL().process(
					new File[] { global }, tempDir, clientType.option, cmd
				);
				zipFiles.put(clientType.option, clientZip);
				logger.info(
					"[ClientServlet] `" + clientType.description +
					"` client library created at : " + 
					clientZip.getAbsolutePath()
				);
			}
		} catch (IOException e) {
			throw new ThriftEEStartupException(
				"[HtmlClientServlet] Problem generating HTML library: " + e.getMessage()
			);
		}
	}
	
	protected void configure() {
		addClientType(Generate.HTML);
		addClientType(Generate.JAVA);
		addClientType(Generate.JS);
		addClientType(Generate.PHP);
	}
	
	protected void addClientType(ThriftCommand.Generate clientType) {
		this.clientTypes.add(clientType);
	}
	
	@Override
	protected File zipFile() {
		//return htmlClientLibrary;
		return null;
	}
}
