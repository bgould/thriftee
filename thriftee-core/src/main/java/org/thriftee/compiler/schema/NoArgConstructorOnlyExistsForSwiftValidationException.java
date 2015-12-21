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
