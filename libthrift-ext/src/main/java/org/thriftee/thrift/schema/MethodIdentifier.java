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
package org.thriftee.thrift.schema;

public final class MethodIdentifier {

  private final String moduleName;

  private final String serviceName;

  private final String methodName;

  public MethodIdentifier(
      final String moduleName, 
      final String serviceName, 
      final String methodName) {
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

  public String getModuleName() {
    return moduleName;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getMethodName() {
    return methodName;
  }

  @Override
  public int hashCode() {
    final int p = 31;
    int result = 1;
    result = p * result + ((methodName == null) ? 0 : methodName.hashCode());
    result = p * result + ((moduleName == null) ? 0 : moduleName.hashCode());
    result = p * result + ((serviceName == null) ? 0 : serviceName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MethodIdentifier other = (MethodIdentifier) obj;
    if (methodName == null) {
      if (other.methodName != null)
        return false;
    } else if (!methodName.equals(other.methodName))
      return false;
    if (moduleName == null) {
      if (other.moduleName != null)
        return false;
    } else if (!moduleName.equals(other.moduleName))
      return false;
    if (serviceName == null) {
      if (other.serviceName != null)
        return false;
    } else if (!serviceName.equals(other.serviceName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "MethodIdentifier ["
        + "moduleName=" + moduleName + ", "
        + "serviceName=" + serviceName + ", "
        + "methodName=" + methodName
    + "]";
  }

}
