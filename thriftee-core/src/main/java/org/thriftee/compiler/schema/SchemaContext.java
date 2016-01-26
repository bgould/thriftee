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

  ServiceSchema resolveService(String serviceName) {
    final int dot = serviceName.indexOf('.');
    if (dot == -1) {
      throw new IllegalArgumentException("service name should have a .");
    }
    final String modulename = serviceName.substring(0, dot);
    final String servicename = serviceName.substring(dot + 1);
    final ModuleSchema module = schema.getModules().get(modulename);
    if (module == null) {
      throw new IllegalArgumentException("unknown module: " + module);
    }
    final ServiceSchema service = module.getServices().get(servicename);
    if (service == null) {
      throw new IllegalArgumentException("unknown service: " + service);
    }
    return service;
  }
}
