package org.thriftee.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.thriftee.servlet.model.DirectoryListingModel;
import org.thriftee.util.FileUtil;

import com.facebook.nifty.processor.NiftyProcessorAdapters;
import com.facebook.swift.service.ThriftEventHandler;
import com.facebook.swift.service.ThriftServiceProcessor;

public abstract class EndpointServlet extends FrameworkServlet {

	private static final long serialVersionUID = -1548842313729559248L;

//	private Map<String, Class<?>> processorMappings = new HashMap<String, Class<?>>();
	
	private Map<String, ThriftServiceProcessor> processors = new HashMap<String, ThriftServiceProcessor>();
	
	private TProtocolFactory protocolFactory;
	
	private File zipDownload;
	
	protected File zipDownload(HttpServletRequest request) throws IOException {
		if (zipDownload == null) {
			synchronized (this) {
				if (zipDownload == null) {
					File output = new File(thrift().tempDir(), "thrift-idl.zip");
					FileUtil.createZipFromDirectory(output, "", thrift().idlDir());
					zipDownload = output;
				}
			}
		}
		return zipDownload;
	}
	
	protected void addProcessor(String name, Object svc) {
		List<ThriftEventHandler> eventList = Collections.emptyList();
		ThriftServiceProcessor proc = new ThriftServiceProcessor(codecManager(), eventList, svc);
		this.processors.put(name, proc);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		File zipDownload = zipDownload(req);
		if ("zip".equalsIgnoreCase(req.getParameter("download")) && zipDownload != null) {
			sendZip(req, resp, zipDownload);
			return;
		}
		
		String pathInfo = req.getPathInfo();
		while (pathInfo != null && pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		if (StringUtils.isBlank(pathInfo)) {
			DirectoryListingModel model = new DirectoryListingModel();
			model.setTitle(getServletName());
			model.setPathPrefix(req.getContextPath() + req.getServletPath() + "/");
			for (String s : processors.keySet()) {
				model.getFiles().put(model.getPathPrefix() + s, s);
			}
			model.setServerLine("ThriftEE");
			if (zipDownload != null) {
				model.getDownloads().put(
					resp.encodeURL(model.getPathPrefix() + "?download=zip"), 
					"[download as a zip file]"
				);
			}
			req.setAttribute("model", model);
			req.getRequestDispatcher("/WEB-INF/thriftee/jsp/directory_listing.jsp").include(req, resp);
		} else if (processors.containsKey(pathInfo)) {
			return;
		}
		
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		getServletContext().log("entering doPost()");
		
		String pathInfo = request.getPathInfo();
		while (pathInfo != null && pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		
		ThriftServiceProcessor processor = processors.get(pathInfo);
		if (processor == null) {
			notFound(request, response);
			return;
		}
		
		TTransport inTransport = null;
		TTransport outTransport = null;
		try {
			response.setContentType("application/json");
			
			InputStream in = request.getInputStream();
			OutputStream out = response.getOutputStream();

			TTransport transport = new TIOStreamTransport(in, out);
			inTransport = transport;
			outTransport = transport;

			TProtocol inProtocol = getInProtocolFactory().getProtocol(inTransport);
			TProtocol outProtocol = getOutProtocolFactory().getProtocol(outTransport);
			TProcessor tprocessor = NiftyProcessorAdapters.processorToTProcessor(processor);
			//RequestContext ctx = new ServletRequestContext(request, inProtocol, outProtocol);
			
			boolean result = tprocessor.process(inProtocol, outProtocol);
			out.flush();
			
				
			/*
			ListenableFuture<Boolean> future = processor.process(inProtocol, outProtocol, ctx);
			try {
				Boolean result = future.get(30, TimeUnit.SECONDS);
				if (!response.isCommitted() && !result) {
					response.setStatus(500);
				}
				outProtocol.writeMessageEnd();
				out.flush();
			} catch (TimeoutException | InterruptedException e) {
				throw new ServletException(e);
			} catch (ExecutionException e) {
				if (e.getCause() != null) {
					throw new ServletException(e.getCause());
				} else {
					throw new ServletException(e);
				}
			}
			*/
		} catch (TException te) {
			throw new ServletException(te);
		} finally {
			if (inTransport != null) {
				inTransport.close();
			}
			if (outTransport != null) {
				outTransport.close();
			}
		}
		
	}

	protected ThriftServiceProcessor getProcessor(String processor) {
		if (processors.containsKey(processor)) {
			return processors.get(processor);
		}
		return null;
	}
	
	protected TProtocolFactory getInProtocolFactory() {
		if (protocolFactory == null) {
			protocolFactory = new TJSONProtocol.Factory();
		}
		return protocolFactory;
	}

	protected TProtocolFactory getOutProtocolFactory() {
		return getInProtocolFactory();
	}
	
	protected void sendZip(final HttpServletRequest request, final HttpServletResponse response, File zipFile) throws IOException, ServletException {
		String mimeType = getServletContext().getMimeType(zipFile.getName());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "application/zip";
		}
		response.setContentType(mimeType);
		response.setContentLength((int) zipFile.length());
		response.setHeader("Content-Disposition", "attachment;filename=\"" + zipFile.getName() + "\"");
		FileInputStream in = null;
		ServletOutputStream sos = null;
		try {
			in = new FileInputStream(zipFile);
			sos = response.getOutputStream();	
			byte[] buffer = new byte[1024];
			for (int n; ((n = in.read(buffer)) > -1); ) {
				sos.write(buffer, 0, n);
			}
		} finally {
			FileUtil.forceClosed(in);
			FileUtil.forceClosed(sos);
		}
	}
	
}
