package com.facebook.swift.parser;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Objects {

  public static ToStringHelper toStringHelper(Object obj) {
    return new ToStringHelper(obj);
  }

  public static class ToStringHelper {
    private Map<String, String> fields = new LinkedHashMap<String,String>();
    private final Object obj;
    public ToStringHelper(Object obj) {
      this.obj = obj;
    }
    public ToStringHelper add(String label, Object value) {
      fields.put(label, value+"");
      return this;
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(obj.getClass().getSimpleName()).append("{");
      for (Iterator<Entry<String,String>> i = fields.entrySet().iterator(); 
            i.hasNext(); ) {
        final Entry<String, String> e = i.next();
        sb.append(e.getKey()).append("=").append(e.getValue());
        if (i.hasNext()) {
          sb.append(", ");
        }
      }
      sb.append("}");
      return sb.toString();
    }
  }
  
}
