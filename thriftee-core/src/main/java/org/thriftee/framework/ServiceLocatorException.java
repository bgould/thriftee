package org.thriftee.framework;

import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftSystemException;

public class ServiceLocatorException extends ThriftSystemException {

  private static final long serialVersionUID = -5694741758963250097L;

  public ServiceLocatorException(Messages msg, Object... args) {
    super(msg, args);
  }

  public ServiceLocatorException(Throwable t, Messages msg, Object... args) {
    super(t, msg, args);
  }

  public static enum Messages implements ThriftMessage {
    SVCLOC_000("A generic error occurred attempting to locate a service."),
    SVCLOC_001("A service has already been registered for: {}"),
    ;
    private final String _message;
    private Messages(String message) {
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
