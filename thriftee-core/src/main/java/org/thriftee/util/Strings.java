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
package org.thriftee.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringEscapeUtils;

public class Strings {

  private Strings() {
  }

  public static final byte[] utf8Bytes(String s) {
    try {
      return s.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF8 apparently is not supported.");
    }
  }
  
  public static boolean isBlank(String s) {
    return (s == null) ? true : s.trim().equals("");
  }
  
  public static boolean isNotBlank(String s) {
    return !isBlank(s);
  }

  public static String trimToNull(Object o) {
    if (o == null) {
    return null;
    }
    String s = o.toString();
    if (s == null) {
    return null;
    }
    s = s.trim();
    if ("".equals(s)) {
    return null;
    }
    return s;
  }
  
  public static String escHtml(String html) {
	return StringEscapeUtils.escapeHtml(html);
  }

}
