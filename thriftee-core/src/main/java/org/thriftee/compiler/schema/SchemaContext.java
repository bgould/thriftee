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

public class SchemaContext {

  private final ThriftSchema schema;

  SchemaContext(ThriftSchema schema) {
    this.schema = schema;
  }

  ThriftSchemaType wrap(ISchemaType type) {
    return new ThriftSchemaType(this, type);
  }

  ModuleSchema resolveModule(String moduleName) {
    final ModuleSchema module = schema.getModules().get(moduleName);
    if (module == null) {
      throw new IllegalArgumentException("unknown module: " + moduleName);
    }
    return module;
  }

  ServiceSchema resolveService(String moduleName, String serviceName) {
    final ModuleSchema module = resolveModule(moduleName);
    final ServiceSchema service = module.getServices().get(serviceName);
    if (service == null) {
      throw new IllegalArgumentException("unknown service: " + service);
    }
    return service;
  }

  ServiceSchema resolveService(String serviceName) {
    final int dot = serviceName.indexOf('.');
    if (dot == -1) {
      throw new IllegalArgumentException("service name should have a .");
    }
    final String modulename = serviceName.substring(0, dot);
    final String servicename = serviceName.substring(dot + 1);
    return resolveService(modulename, servicename);
  }

  ISchemaType resolveReference(SchemaReference reference) {
    final ISchemaType resolved;
    final ModuleSchema module = resolveModule(reference.getModuleName());
    switch (reference.getReferenceType()) {
    case EXCEPTION:
      resolved = module.getExceptions().get(reference.getTypeName());
      break;
    case TYPEDEF:
      resolved = module.getTypedefs().get(reference.getTypeName());
      break;
    case STRUCT:
      resolved = module.getStructs().get(reference.getTypeName());
      break;
    case UNION:
      resolved = module.getUnions().get(reference.getTypeName());
      break;
    case ENUM:
      resolved = module.getEnums().get(reference.getTypeName());
      break;
    default:
      throw new IllegalStateException();
    }
    if (resolved == null) {
      throw new IllegalArgumentException("Unable to resolve: " + reference);
    }
    return resolved;
  }

  ISchemaType resolveType(final ISchemaType type) {
    return resolveType(type.getModuleName(), type.getTypeName());
  }

  ISchemaType resolveType(String moduleName, String typeName) {
    final ModuleSchema module = resolveModule(moduleName);
    ISchemaType result = module.getStructs().get(typeName);
    if (result == null) {
      result = module.getUnions().get(typeName);
      if (result == null) {
        result = module.getExceptions().get(typeName);
        if (result == null) {
          result = module.getEnums().get(typeName);
          if (result == null) {
            result = module.getTypedefs().get(typeName);
            if (result == null) {
              throw new IllegalArgumentException(
                moduleName + "." + typeName + " not found"
              );
            }
          }
        }
      }
    }
    return result;
  }

  AbstractStructSchema<?, ?, ?, ?> resolveStructSchema(ISchemaType type) {
    if (type instanceof AbstractStructSchema<?, ?, ?, ?>) {
      return (AbstractStructSchema<?, ?, ?, ?>) type;
    }
    final String moduleName = type.getModuleName();
    final String typeName = type.getTypeName();
    final ISchemaType resolved = resolveType(moduleName, typeName);
    if (resolved instanceof AbstractStructSchema<?, ?, ?, ?>) {
      return (AbstractStructSchema<?, ?, ?, ?>) resolved;
    } else {
      throw new IllegalArgumentException(
        moduleName + "." + typeName + ", found but was not a struct type."
      );
    }
  }

}
