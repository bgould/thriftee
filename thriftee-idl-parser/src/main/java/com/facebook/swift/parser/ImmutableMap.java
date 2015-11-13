package com.facebook.swift.parser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImmutableMap {

  public static <K,V> Map<K,V> copyOf(Map<K,V> map) {
    Map<K,V> result = new LinkedHashMap<K,V>(map);
    return Collections.unmodifiableMap(result);
  }

}
