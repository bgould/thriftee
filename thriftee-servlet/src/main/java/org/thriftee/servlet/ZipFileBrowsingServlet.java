package org.thriftee.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.thriftee.servlet.model.DirectoryListingModel;
import org.thriftee.util.FileUtil;

public abstract class ZipFileBrowsingServlet extends FrameworkServlet {

	private static final long serialVersionUID = 725460026573872682L;

	protected abstract File zipFile();
	
	protected boolean canDownload(HttpServletRequest request) {
		return true;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		File zipFile = zipFile();
		
		if ("zip".equalsIgnoreCase(request.getParameter("download")) && canDownload(request)) {
			sendZip(request, response);
			return;
		}
		
		String requestPath = request.getPathInfo();
		if (requestPath != null && requestPath.startsWith("/")) {
			requestPath = requestPath.substring(1);
		}
		FileInputStream fileIn = null;
		ZipInputStream zipIn = null;
		try {
			fileIn = new FileInputStream(zipFile);
			zipIn = new ZipInputStream(fileIn);
			if (StringUtils.isBlank(requestPath)) {
				sendDirectory(request, response, zipIn, "");
				return;
			}
			for (ZipEntry zipEntry; (zipEntry = zipIn.getNextEntry()) != null; ) {
				if (zipEntry.getName().equals(requestPath) || zipEntry.getName().equals(requestPath + "/")) {
					if (!zipEntry.isDirectory()) {
						sendFile(request, response, zipIn, zipEntry);
						return;
					} else {
						sendDirectory(request, response, zipIn, requestPath);
						return;
					}
				}
			}
		} finally {
			FileUtil.forceClosed(zipIn);
			FileUtil.forceClosed(fileIn);
		}
		
		notFound(request, response);
	}
	
	protected void sendZip(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
		File zipFile = zipFile();
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
	
	protected void sendDirectory(final HttpServletRequest request, final HttpServletResponse response, final ZipInputStream in, final String matchedPath) 
			throws IOException, ServletException {
		if (!request.getRequestURI().endsWith("/")) {
			response.sendRedirect(request.getRequestURI() + "/");
			return;
		}

		final String pathPrefix = request.getContextPath() + request.getServletPath() + "/";
		final DirectoryListingModel model = new DirectoryListingModel();
		final SortedMap<String, String> files = model.getFiles();
		model.setPathPrefix(pathPrefix);
		model.setTitle(pathPrefix + matchedPath);
		model.setServerLine("ThriftEE");
		if (canDownload(request)) {
			model.getDownloads().put(
				response.encodeURL(pathPrefix + "?download=zip"), 
				"[download as a zip file]"
			);
		}
		
		if (StringUtils.isNotBlank(matchedPath)) {
			String levelUp = matchedPath.substring(0, matchedPath.length() - 1);
			int index = levelUp.lastIndexOf('/');
			levelUp = index > -1 ? levelUp.substring(0, index + 1) : "";
			files.put(pathPrefix + levelUp, "..");
		} else {
			files.put(pathPrefix + matchedPath, ".");
		}
		for (ZipEntry entry; (entry = in.getNextEntry()) != null && entry.getName().startsWith(matchedPath); ) {
			String remaining = entry.getName().substring(matchedPath.length());
			int slashIndex = remaining.indexOf('/');
			if ((slashIndex == -1) || (entry.isDirectory() && slashIndex == (remaining.length() - 1))) {
				files.put(pathPrefix + entry.getName(), remaining);
			}
		}
		
		request.setAttribute("model", model);
		request.getRequestDispatcher("/WEB-INF/thriftee/jsp/directory_listing.jsp").include(request, response);
	}
	
	protected void sendFile(HttpServletRequest request, HttpServletResponse response, ZipInputStream in, ZipEntry entry)
			throws ServletException, IOException {
		String mimeType = getServletContext().getMimeType(entry.getName());
		if (StringUtils.isBlank(mimeType)) {
			mimeType = "text/plain";
		}
		response.setContentType(mimeType);
		response.setContentLength((int) entry.getSize());
		ServletOutputStream sos = response.getOutputStream();
		try {
			byte[] buffer = new byte[1024];
			for (int n; ((n = in.read(buffer)) > -1); ) {
				sos.write(buffer, 0, n);
			}
		} finally {
			FileUtil.forceClosed(sos);
		}
	}
	
}
