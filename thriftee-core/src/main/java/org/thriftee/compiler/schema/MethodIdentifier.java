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

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class MethodIdentifier {

  public static final int THRIFT_INDEX_MODULE_NAME = 1;

  public static final int THRIFT_INDEX_SERVICE_NAME = THRIFT_INDEX_MODULE_NAME + 1;

  public static final int THRIFT_INDEX_METHOD_NAME = THRIFT_INDEX_SERVICE_NAME + 1;

  private final String moduleName;

  private final String serviceName;

  private final String methodName;

  public MethodIdentifier() throws NoArgConstructorOnlyExistsForSwiftValidationException {
    throw new NoArgConstructorOnlyExistsForSwiftValidationException();
  }

  public MethodIdentifier(String moduleName, String serviceName, String methodName) {
    super();
    if (moduleName == null) {
      throw new IllegalArgumentException("moduleName cannot be null.");
    }
    if (serviceName == null) {
      throw new IllegalArgumentException("serviceName cannot be null.");
    }
    if (methodName == null) {
      throw new IllegalArgumentException("methodName cannot be null.");
    }
    this.moduleName = moduleName;
    this.serviceName = serviceName;
    this.methodName = methodName;
  }

  @ThriftField((short)THRIFT_INDEX_MODULE_NAME)
  public String getModuleName() {
    return moduleName;
  }

  @ThriftField((short)THRIFT_INDEX_SERVICE_NAME)
  public String getServiceName() {
    return serviceName;
  }

  @ThriftField((short)THRIFT_INDEX_METHOD_NAME)
  public String getMethodName() {
    return methodName;
  }

}
