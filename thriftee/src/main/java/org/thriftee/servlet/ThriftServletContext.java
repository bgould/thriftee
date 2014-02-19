package org.thriftee.servlet;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.thriftee.framework.ThriftEE;
import org.thriftee.framework.ThriftEEConfig;
import org.thriftee.framework.ThriftEEFactory;
import org.thriftee.framework.WarFileScannotationConfigurator;
import org.thriftee.framework.ThriftEEConfig.Builder;

public class ThriftServletContext {
	
	public static final String THRIFT_EXECUTABLE_PARAM = "thrift.executable";
	
	public static final String THRIFT_LIB_DIR_PARAM = "thrift.lib.dir";
	
	private final WeakReference<ServletContext> servletContextHolder;
	
	private ThriftServletContext(ServletContext ctx) {
		servletContextHolder = new WeakReference<ServletContext>(ctx);
	}
	
	public ServletContext getServletContext() {
		final ServletContext ctx = servletContextHolder.get();
		if (ctx == null) {
			throw new RuntimeException("ServletContext has been garbage collected.");
		}
		return ctx;
	}
	
	public ThriftEE getThriftServices() {
		return servicesFor(getServletContext());
	}
	
	private static String THRIFT_SERVICES_ATTR = ThriftServletContext.class.getName();
	
    public static void initialize(ServletContext ctx) {
    	if (ctx.getAttribute(THRIFT_SERVICES_ATTR) instanceof ThriftEE) {
    		throw new IllegalStateException(
				"ThriftServices is already initialized for this context.");
    	}
    	File tempDir = (File) ctx.getAttribute(ServletContext.TEMPDIR);
		ThriftEEConfig config = new ThriftEEConfig.Builder().
											tempDir(tempDir).
											thriftExecutable(readThriftExecutable(ctx)).
											thriftLibDir(readThriftLibDir(ctx)).
											scannotationConfigurator(new WarFileScannotationConfigurator(ctx)).
											build();
		ThriftEE svcs = new ThriftEEFactory().create(config);
		ctx.setAttribute(THRIFT_SERVICES_ATTR, svcs);
    }
    
    private static File readThriftExecutable(ServletContext ctx) {
    	String executable = ctx.getInitParameter(THRIFT_EXECUTABLE_PARAM);
    	if (executable != null) {
    		return new File(executable);
    	} else {
    		return null;
    	}
    }
    
    private static File readThriftLibDir(ServletContext ctx) {
    	String libDir = ctx.getInitParameter(THRIFT_LIB_DIR_PARAM);
    	if (libDir != null) {
    		return new File(libDir);
    	} else {
    		return null;
    	}
    }
    
    public static void destroy(ServletContext ctx) {
//    	if (ctx.getAttribute(THRIFT_SERVICES_ATTR) instanceof ThriftEE) {
//    		throw new IllegalStateException(
//				"ThriftServices is already initialized for this context.");
//    	}
    }
    
    public static ThriftEE servicesFor(ServletContext ctx) {
    	ThriftEE svcs = (ThriftEE) ctx.getAttribute(THRIFT_SERVICES_ATTR);
    	if (svcs == null) {
    		throw new IllegalStateException("ThriftServletContext has not been initialized.");
    	}
    	return svcs;
    }
    
    public static class Listener implements ServletContextListener {
	
		/**
	     * @see ServletContextListener#contextInitialized(ServletContextEvent)
	     */
	    public void contextInitialized(ServletContextEvent event) {
	    	initialize(event.getServletContext());
	    }
	    	
		/**
	     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	     */
	    public void contextDestroyed(ServletContextEvent event) {
	    	destroy(event.getServletContext());
	    }
    
    }
	
}
