package org.thriftee.servlet.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DirectoryListingModel {
	
	private final Map<String, String> downloads = new LinkedHashMap<String, String>();
	
	private final SortedMap<String, String> files = new TreeMap<String, String>();
	
	private String title;
	
	private String serverLine;
	
	private String pathPrefix;
	
	public Map<String, String> getDownloads() {
		return downloads;
	}

	public SortedMap<String, String> getFiles() {
		return files;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getServerLine() {
		return serverLine;
	}

	public void setServerLine(String serverLine) {
		this.serverLine = serverLine;
	}

	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}
	
}
