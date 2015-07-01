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
