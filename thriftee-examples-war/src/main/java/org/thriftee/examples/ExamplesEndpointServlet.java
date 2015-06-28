package org.thriftee.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;

import org.thriftee.servlet.ThriftEEServlet;

@WebServlet(
  urlPatterns = { "/services/*" }, 
  loadOnStartup = 1, 
  name = "Example Service Endpoints"
)
public class ExamplesEndpointServlet extends ThriftEEServlet {

  private static final long serialVersionUID = 920163052774234943L;

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public void init() throws ServletException {
    super.init();
    LOG.info("==== init'd ExamplesEndpointServlet ====");
  }

}
