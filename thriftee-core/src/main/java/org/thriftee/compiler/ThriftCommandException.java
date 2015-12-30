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

    COMMAND_301("Thrift command failed: %s"),
    COMMAND_302("Thrift command did not return an exit code of 0: %s"),
    COMMAND_303("Thrift command was interrupted: %s"),
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
