package org.thriftee.examples;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.thriftee.examples.presidents.PresidentService;
import org.thriftee.servlet.EndpointServlet;

@WebServlet(
	urlPatterns={"/endpoints/*"}, 
	loadOnStartup=1,
	name="Example Service Endpoints"
)
public class ExamplesEndpointServlet extends EndpointServlet {

	private static final long serialVersionUID = 920163052774234943L;

	@EJB 
	private PresidentService presidentService;

	@Override
	public void init() throws ServletException {
		addProcessor("PresidentService", presidentService);
	}
	
}
