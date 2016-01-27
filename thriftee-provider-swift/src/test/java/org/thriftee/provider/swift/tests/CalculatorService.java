package org.thriftee.provider.swift.tests;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface CalculatorService {

  @ThriftMethod
  public int calculate(Work work) throws CalculatorException;

}
