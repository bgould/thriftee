package org.thriftee.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.ext.servlet.ServerServlet;
import org.thriftee.framework.ThriftEE;
import org.thriftee.restlet.FrameworkResource;

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
  }

  @Override
  protected void init(Component component) {
    super.init(component);
    FrameworkResource.initComponent(component);
  }

  @Override
  protected void init(Application app) {
    super.init(app);
    final ServletContext ctx = getServletContext();
    final ThriftEE thrift = ThriftServletContext.servicesFor(ctx);
    FrameworkResource.initApplication(app, thrift);
  }

}
