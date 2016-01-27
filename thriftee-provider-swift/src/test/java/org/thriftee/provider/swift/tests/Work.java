package org.thriftee.provider.swift.tests;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Work {

  @ThriftField(1)
  public Op operation;

  @ThriftField(2)
  public int operand1;

  @ThriftField(3)
  public int operand2;

}
