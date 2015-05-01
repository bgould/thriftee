package org.thriftee.compiler;

import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftRuntimeException;

public class ThriftCommandException extends ThriftRuntimeException {

  public ThriftCommandException(ThriftMessage msg, Object... args) {
    super(msg, args);
  }
  
  public ThriftCommandException(
      Throwable cause, ThriftMessage msg, Object... args) {
    super(cause, msg, args);
  }

  public static enum ThriftCommandMessage implements ThriftMessage {
    
    COMMAND_101("Thrift 'version' command failed: %s"),
    COMMAND_102("Thrift 'version' command did not return an exit code of 0: %s"),
    COMMAND_103("Thrift 'version' command was interrupted: %s"),
    
    COMMAND_201("Thrift 'help' command failed: %s"),
    COMMAND_202("Thrift 'help' command did not return an exit code of 0: %s"),
    COMMAND_203("Thrift 'help' command was interrupted: %s"),
    ;

    private final String _message;
    
    private ThriftCommandMessage(String message) {
      this._message = message;
    }
    
    @Override
    public String getCode() {
      return name();
    }

    @Override
    public String getMessage() {
      return _message;
    }
    
  }
  
  private static final long serialVersionUID = 1610656228465185536L;

}
