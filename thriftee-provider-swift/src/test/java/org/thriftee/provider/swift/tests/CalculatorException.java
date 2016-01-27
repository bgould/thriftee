package org.thriftee.provider.swift.tests;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class CalculatorException extends Exception {

  private static final long serialVersionUID = -8414042840601349968L;

  @ThriftConstructor
  public CalculatorException(@ThriftField String message) {
    super(message);
  }

  @ThriftField(1)
  public String getMessage() {
    return super.getMessage();
  }

}
