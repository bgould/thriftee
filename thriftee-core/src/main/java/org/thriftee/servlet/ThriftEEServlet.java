package org.thriftee.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.servlet.ServerServlet;
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

  public static void initComponent(Component component) {
    component.getClients().add(Protocol.FILE);
    component.getClients().add(Protocol.CLAP);
    component.getClients().add(Protocol.ZIP);
  }

  @Override
  public void init() throws ServletException {
    super.init();
  }

  @Override
  protected void init(Component component) {
    super.init(component);
    initComponent(component);
  }

  @Override
  protected void init(Application app) {
    super.init(app);
    app.getContext().getAttributes().put(
      FrameworkResource._attr2, 
      ThriftServletContext.servicesFor(getServletContext())
    );
  }

}
