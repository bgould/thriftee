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

public class ThriftStartupException extends ThriftSystemException {

  private static final long serialVersionUID = 4082854407680864687L;

  public ThriftStartupException(ThriftMessage msg, Object... args) {
    super(msg, args);
  }

  public ThriftStartupException(Throwable t, ThriftMessage msg, Object... args) {
    super(t, msg, args);
  }

  public static enum ThriftStartupMessage implements ThriftMessage {

    STARTUP_001("A problem occurred exporting IDL: %s"),
    STARTUP_002("A problem occurred scanning the Swift annotations at startup: %s"),
    STARTUP_003("A problem occurred parsing generated IDL at startup: %s"),
    STARTUP_004("Generated IDL did not contain a global.thrift file"),
    STARTUP_005("Thrift library directory does not exist: %s"),
    STARTUP_006("Thrift library directory exists but appears invalid: %s"),
    STARTUP_007("Thrift executable not specified, and not found on path."),
    STARTUP_008("Could not get Thrift version string from executable: %s"),
    STARTUP_009("Error generating client library %s: %s"),
    STARTUP_010("Error locating implementation for service %s: %s"),
    STARTUP_011("Error exporting XML schema: %s"),
    STARTUP_012("An error occurred while validating model against XSD: %s"),
    STARTUP_013("Validation of model against XSD failed: %s"),
    STARTUP_014("An error occurred generated XML artifacts: %s"),
    STARTUP_015("An error occurred unzipped Thrift libraries: %s"),
    STARTUP_016("A problem occurred while building processor map.")
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
