/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.restlet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
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
  
  @XmlElement
  public Map<String, String> getDownloads() {
    return downloads;
  }

  @XmlElement
  public SortedMap<String, String> getFiles() {
    return files;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @XmlElement
  public String getServerLine() {
    return serverLine;
  }

  public void setServerLine(String serverLine) {
    this.serverLine = serverLine;
  }

  @XmlElement
  public String getPathPrefix() {
    return pathPrefix;
  }

  public void setPathPrefix(String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

}
