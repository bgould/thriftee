package org.thriftee.framework;

import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftSystemException;

public class ThriftStartupException extends ThriftSystemException {

  private static final long serialVersionUID = 4082854407680864687L;

  public ThriftStartupException(ThriftMessage msg, Object... args) {
    super(msg, args);
  }

  public ThriftStartupException(Throwable t, ThriftMessage msg, Object... args) {
    super(t, msg, args);
  }
  
  public static enum ThriftStartupMessage implements ThriftMessage {
 
    STARTUP_001("A problem occurred writing the IDL for the annotated classes: %s"),
    STARTUP_002("A problem occurred scanning the Swift annotations at startup: %s"),
    STARTUP_003("A problem occurred parsing generated IDL at startup: %s"),
    STARTUP_004("Generated IDL did not contain a global.thrift file"),
    STARTUP_005("Thrift library directory does not exist: %s"),
    STARTUP_006("Thrift library directory exists but appears invalid: %s"),
    STARTUP_007("Thrift executable specified, but does not exist: %s"),
    STARTUP_008("Could not get Thrift version string from executable: %s"),
    STARTUP_009("Error generating client library %s: %s"),
    STARTUP_010("Error locating implementation for service %s: %s"),
    ;

    private final String _message;
    
    private ThriftStartupMessage(String message) {
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
  
}
