package org.thriftee.compiler.schema;

import org.thriftee.exceptions.ThriftMessage;

public class NoArgConstructorOnlyExistsForSwiftValidationException extends SchemaBuilderException {

  private static final long serialVersionUID = 3842118639398735238L;

  public NoArgConstructorOnlyExistsForSwiftValidationException() {
    super(new ThriftMessage() {

      private static final long serialVersionUID = 4173386200771028793L;

      @Override
      public String getMessage() {
        return "This method only exists because swift requires either "
             + "a no-arg constructor or a factory method constructor.  "
             + "Please use the schema builder framework for constructing "
             + "instances of this class.";
      }
      
      @Override
      public String getCode() {
        return "NO_ARG_CONSTRUCTOR_HACK";
      }
    });
  }
  
}
