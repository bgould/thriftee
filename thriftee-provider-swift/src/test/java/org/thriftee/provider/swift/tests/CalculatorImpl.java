package org.thriftee.provider.swift.tests;

public class CalculatorImpl implements CalculatorService {

  @Override
  public int calculate(Work work) throws CalculatorException {
    try {
      switch (work.operation) {
        case ADD:
          return work.operand1 + work.operand2;
        case SUBTRACT:
          return work.operand1 - work.operand2;
        case MULTIPLY:
          return work.operand1 * work.operand2;
        case DIVIDE:
          return work.operand1 / work.operand2;
        default:
          throw new CalculatorException("unknown operation: " + work.operation);
      }
    } catch (CalculatorException e) {
      throw e;
    } catch (Exception e) {
      throw new CalculatorException(e.getMessage());
    }
  }

}
