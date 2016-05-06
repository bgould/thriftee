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

import java.io.Serializable;

public final class SchemaReference implements Serializable {

  private static final long serialVersionUID = 335224512770544907L;

  private final SchemaReference.Type referenceType;
  
  private final String moduleName;
  
  private final String typeName;

  public static SchemaReference referTo(
      final SchemaReference.Type referenceType, 
      final String moduleName, 
      final String typeName) {
    return new SchemaReference(referenceType, moduleName, typeName);
  }

  protected SchemaReference(
      final SchemaReference.Type referenceType,
      final String moduleName, 
      final String typeName) {
    super();
    this.referenceType = referenceType;
    this.moduleName = moduleName;
    this.typeName = typeName;
  }

  public String getModuleName() {
    return this.moduleName;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public SchemaReference.Type getReferenceType() {
    return referenceType;
  }

  public static enum Type {
    EXCEPTION,
    TYPEDEF,
    STRUCT,
    UNION,
    ENUM,
    ;
  }

  @Override
  public String toString() {
    return "SchemaReference ["
      + "referenceType=" + referenceType + ", "
      + "moduleName=" + moduleName + ", "
      + "typeName=" + typeName + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime*result+((moduleName == null)?0:moduleName.hashCode());
    result = prime*result+((referenceType == null)?0:referenceType.hashCode());
    result = prime*result+((typeName == null)?0:typeName.hashCode());
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
    SchemaReference other = (SchemaReference) obj;
    if (moduleName == null) {
      if (other.moduleName != null)
        return false;
    } else if (!moduleName.equals(other.moduleName))
      return false;
    if (referenceType != other.referenceType)
      return false;
    if (typeName == null) {
      if (other.typeName != null)
        return false;
    } else if (!typeName.equals(other.typeName))
      return false;
    return true;
  }

  public String toNamespacedIDL(String namespace) {
    if (namespace != null && getModuleName() != null && 
          namespace.equals(getModuleName())) {
      return getTypeName();
    } else {
      return getModuleName() + "." + getTypeName();
    }
  }

}
