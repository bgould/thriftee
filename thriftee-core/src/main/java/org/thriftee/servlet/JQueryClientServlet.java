package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

@WebServlet("/clients/jquery/*")
public class JQueryClientServlet extends ZipFileBrowsingServlet {

    private static final long serialVersionUID = -3518542031465043696L;

    private File jqueryClientLibrary;
    
    @Override
    public void init() throws ServletException {
        logger.info("[JQueryClientServlet] Generating jQuery client library");
        try {
            ThriftCommand cmd = new ThriftCommand(Generate.JS);
            cmd.addFlag(Flag.JS_JQUERY);
            cmd.setRecurse(true);
            cmd.setVerbose(true);
            if (thrift().thriftExecutable() != null) {
                cmd.setThriftCommand(thrift().thriftExecutable().getAbsolutePath());
            }
            final File[] extraDirs;
            if (thrift().thriftLibDir() != null) {
                File jsLib = new File(thrift().thriftLibDir(), "js/src");
                extraDirs = new File[] { jsLib };
            } else {
                extraDirs = new File[0];
            }
            jqueryClientLibrary = new ProcessIDL().process(
                new File[] { thrift().globalIdlFile() }, thrift().tempDir(), "jquery-client", cmd, extraDirs
            );
            logger.info(
                "[JQueryClientServlet] jQuery client library created at : " + 
                jqueryClientLibrary.getAbsolutePath()
            );
        } catch (IOException e) {
            throw new ServletException(
                "[JQueryClientServlet] Problem generating jQuery library: " + 
                e.getMessage(), e
            );
        }
    }
    
    @Override
    protected File zipFile() {
        return jqueryClientLibrary;
    }
    
}
