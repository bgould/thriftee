package com.facebook.swift.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableList {

  public static <T> List<T> copyOf(List<T> list) {
    List<T> result = new ArrayList<T>(list);
    return Collections.unmodifiableList(result);
  }

}
