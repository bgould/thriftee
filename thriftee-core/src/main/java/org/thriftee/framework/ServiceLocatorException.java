package org.thriftee.framework;

import org.thriftee.exceptions.ThriftMessage;
import org.thriftee.exceptions.ThriftSystemException;

public class ServiceLocatorException extends ThriftSystemException {

  private static final long serialVersionUID = -5694741758963250097L;

  public ServiceLocatorException(
      ThriftMessage thrifteeMessage, Object... arguments) {
    super(thrifteeMessage, arguments);
  }

  public ServiceLocatorException(
      Throwable cause, ThriftMessage thrifteeMessage, Object... arguments) {
    super(cause, thrifteeMessage, arguments);
  }

  public static enum ServiceLocatorMessage implements ThriftMessage {
    SVCLOC_000("A generic error occurred attempting to locate a service."),
    ;
    private final String _message;
    private ServiceLocatorMessage(String message) {
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
