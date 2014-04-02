package org.thriftee.examples;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thriftee.examples.presidents.President;
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
	
	//@EJB
	//private UserService userService;
	
	//@EJB
	//private GroupService groupService;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		out.println("<!doctype html><html><head><title>Test Page</title></head><body>");
		
		List<President> presidents = presidentService.getPresidents();
		out.println("<h1>Presidents</h1>");
		out.println("<table class=\"table\" border=\"1\"><thead><tr>");
		out.println("<th>ID</th>");
		out.println("<th>Name</th>");
		out.println("<th>Born</th>");
		out.println("<th>Died</th>");
		out.println("<th>Education</th>");
		out.println("<th>Political Party</th>");
		out.println("<th>Term</th>");
		out.println("</tr></thead><tbody>");
		for (President president : presidents) {
			
			out.println("<tr>");
			
			out.println("<td>");
			out.println(president.getId());
			out.println("</td>");
			
			out.println("<td>");
			out.println(president.getName().getFullName());
			out.println("</td>");
			
			out.println("<td>");
			out.println(president.getBorn());
			out.println("</td>");
			
			out.println("<td>");
			out.println(president.getDied());
			out.println("</td>");
			
			out.println("<td>");
			out.println(president.getEducation());
			out.println("</td>");
			
			out.println("<td>");
			out.println(president.getPoliticalParty());
			out.println("</td>");
			
			out.println("<td>");
			out.println(president.getTerm());
			out.println("</td>");
			
			out.println("</tr>");
			
		}
		out.println("</tbody></table>");
		
		out.println("</body></html>");
	}
	
	@Override
	public void init() throws ServletException {
		addProcessor("PresidentService", presidentService);
		//addProcessor("UserService", userService);
		//addProcessor("GroupService", groupService);
	}
	
}
