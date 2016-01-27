package org.thriftee.provider.swift.tests;

import com.facebook.swift.codec.ThriftEnum;

@ThriftEnum
public enum Op {

  ADD,
  SUBTRACT,
  MULTIPLY,
  DIVIDE;

}
