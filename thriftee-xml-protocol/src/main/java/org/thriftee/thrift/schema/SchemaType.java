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

public interface SchemaType {

  public String getModuleName();

  public String getTypeName();

  public ThriftProtocolType getProtocolType();

  public SchemaType getTrueType();

  public String toNamespacedIDL(String namespace);

  public <T extends SchemaType> T castTo(Class<T> schemaTypeClass);

  public static class Utils {
    private Utils() {}
    public static boolean isPrimitive(SchemaType _schemaType) {
      switch (_schemaType.getProtocolType()) {
      case BOOL:
      case BYTE:
      case DOUBLE:
      case I16: 
      case I32: 
      case I64: 
      case STRING: 
        return true;
      case MAP: 
      case SET: 
      case LIST: 
      case STRUCT: 
      case ENUM:
      case UNKNOWN:
      default:
        return false;
      }
    }
  }
}
