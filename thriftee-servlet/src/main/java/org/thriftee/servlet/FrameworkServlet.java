package org.thriftee.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thriftee.framework.ThriftEE;

import com.facebook.swift.codec.ThriftCodecManager;

public abstract class FrameworkServlet extends HttpServlet {

	private static final long serialVersionUID = -6811436561035251834L;
	
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	protected ThriftEE thrift() {
		return ThriftServletContext.servicesFor(getServletContext());
	}
	
	protected ThriftCodecManager codecManager() {
		return thrift().codecManager();
	}
	
	protected void notFound(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.sendError(404);
	}
	
}
