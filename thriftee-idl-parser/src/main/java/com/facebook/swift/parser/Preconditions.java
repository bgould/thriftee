package com.facebook.swift.parser;

public class Preconditions {

  public static <T> T checkNotNull(final T obj, final String label) {
    if (obj == null) {
      throw new IllegalArgumentException(label + " cannot be null!");
    }
    return obj;
  }

  public static <T> T checkNotNull(final T obj, final String fmt, final Object... args) {
    if (obj == null) {
      throw new IllegalArgumentException(String.format(fmt, args));
    }
    return obj;
  }

}
