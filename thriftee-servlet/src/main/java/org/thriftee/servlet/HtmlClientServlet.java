package org.thriftee.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;

@WebServlet("/clients/html/*")
public class HtmlClientServlet extends ZipFileBrowsingServlet {

    private static final long serialVersionUID = -3518542031465043696L;

    private File htmlClientLibrary;

    @Override
    public void init() throws ServletException {
        logger.info("[HtmlClientServlet] Generating HTML client library");
        try {
            ThriftCommand cmd = new ThriftCommand(Generate.HTML);
            cmd.setRecurse(true);
            if (thrift().thriftExecutable() != null) {
                cmd.setThriftCommand(thrift().thriftExecutable().getAbsolutePath());
            }
            //cmd.addFlag(Flag.HTML_STANDALONE);
            htmlClientLibrary = new ProcessIDL().process(
                new File[] { thrift().globalIdlFile() }, 
                thrift().tempDir(),
                "html-client",
                cmd
            );
            logger.info(
                "[HtmlClientServlet] HTML client library created at : " + 
                htmlClientLibrary.getAbsolutePath()
            );
        } catch (IOException e) {
            throw new ServletException(
                "[HtmlClientServlet] Problem generating HTML library: " + 
                e.getMessage(), e
            );
        }
    }
    
    @Override
    protected File zipFile() {
        return htmlClientLibrary;
    }
}
