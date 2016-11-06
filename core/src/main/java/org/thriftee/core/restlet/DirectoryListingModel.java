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
package org.thriftee.core.restlet;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@XmlRootElement
public class DirectoryListingModel implements DomSerializable {

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

  @Override
  public <T extends Node> T writeToDom(T node) {
    final Document doc = (node instanceof Document) ? (Document) node : node.getOwnerDocument();
    final Element root = doc.createElement("directoryListingModel");
    final Element title = doc.createElement("title");
    final Element baseRef = doc.createElement("baseRef");
    final Element pathPrefix = doc.createElement("pathPrefix");
    final Element serverLine = doc.createElement("serverLine");
    doc.appendChild(root);
    root.appendChild(setText(title, getTitle()));
    root.appendChild(setText(baseRef, getBaseRef()));
    root.appendChild(setText(pathPrefix, getPathPrefix()));
    root.appendChild(setText(serverLine, getServerLine()));
    map(root, "files", getFiles());
    map(root, "downloads", getDownloads());
    return node;
  }

  private static final Element setText(final Element element, String value) {
    element.setTextContent(value);
    return element;
  }

  private static final void map(Node node, String name, Map<String, String> map) {
    if (map != null && !map.isEmpty()) {
      final Document doc = node.getOwnerDocument();
      final Element element = doc.createElement(name);
      for (final Entry<String, String> file : map.entrySet()) {
        addEntry(element, file);
      }
      node.appendChild(element);
    }
  }

  private static final void addEntry(Element el, Entry<String, String> e) {
    final Document doc = el.getOwnerDocument();
    final Element key = doc.createElement("key");
    final Element value = doc.createElement("value");
    final Element entry = doc.createElement("entry");
    el.appendChild(entry);
    entry.appendChild(setText(key, e.getKey()));
    entry.appendChild(setText(value, e.getValue()));
  }
}
