/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
