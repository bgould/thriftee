package org.thriftee.restlet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DirectoryListingModel {

  private final Map<String, String> downloads = new LinkedHashMap<>();

  private final SortedMap<String, String> files = new TreeMap<>();

  private String title;

  private String baseRef;

  private String serverLine;
  
  private String pathPrefix;

  public void setBaseRef(String baseRef) {
    this.baseRef = baseRef;
  }

  public String getBaseRef() {
    return baseRef;
  }
  
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
