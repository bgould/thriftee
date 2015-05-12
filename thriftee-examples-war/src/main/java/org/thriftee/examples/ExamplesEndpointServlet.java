package org.thriftee.examples;

import javax.servlet.annotation.WebServlet;

import org.thriftee.servlet.ThriftEEServlet;

@WebServlet(
  urlPatterns = { "/services/*" }, 
  loadOnStartup = 1, 
  name = "Example Service Endpoints"
)
public class ExamplesEndpointServlet extends ThriftEEServlet {

  private static final long serialVersionUID = 920163052774234943L;

}
