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

import static java.lang.System.out;
import static java.util.Arrays.asList;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

  public static String trimToEmpty(Object o) {
    final String s = (o != null) ? o.toString() : null;
    return s == null ? "" : s.trim();
  }

  public static String[] split(CharSequence s, char c) {
    if (s == null) {
      return new String[0];
    }
    final List<String> parts = new LinkedList<>();
    for (int begin = 0, end = 0, stop = s.length(); end <= stop; end++) {
      if (end == stop || s.charAt(end) == c) {
        parts.add(s.subSequence(begin, end).toString());
        begin = end + 1;
      }
    }
    return parts.toArray(new String[parts.size()]);
  }

  public static String join(Iterable<? extends CharSequence> parts, char glue) {
    final StringBuilder sb = new StringBuilder();
    final Iterator<? extends CharSequence> it = parts.iterator();
    while (it.hasNext()) {
      final CharSequence part = it.next();
      sb.append(part);
      if (it.hasNext()) {
        sb.append(glue);
      }
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    out.println(asList(split("  hello world ", ' ')));
    out.println(asList(split("hello world", ' ')));
    out.println(join(asList(new String[] { "hello", "cruel", "world" }), ' '));
  }

}
