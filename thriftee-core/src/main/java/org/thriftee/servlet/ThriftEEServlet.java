package org.thriftee.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.restlet.data.Protocol;
import org.restlet.ext.servlet.ServerServlet;

/*
 * @author bcg
 */
@WebServlet(
  value = "/thriftee/*",
  loadOnStartup = 1,
  initParams = {
    @WebInitParam(
      name = "org.restlet.application", 
      value = "org.thriftee.restlet.ThriftApplication"
    )
  }
)
public class ThriftEEServlet extends ServerServlet {

  private static final long serialVersionUID = 9217322620918070877L;

  @Override
  public void init() throws ServletException {
    super.init();
    getComponent().getClients().add(Protocol.CLAP);
  }

}
