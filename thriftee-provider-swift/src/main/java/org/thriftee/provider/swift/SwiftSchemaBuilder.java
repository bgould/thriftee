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
package org.thriftee.provider.swift;

import static org.thriftee.compiler.schema.SchemaBuilderException.Messages.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.schema.SchemaBuilder;
import org.thriftee.compiler.schema.SchemaBuilderException;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.compiler.schema.ThriftSchema.Builder;

import com.facebook.swift.parser.ThriftIdlParser;
import com.facebook.swift.parser.model.Document;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class SwiftSchemaBuilder implements SchemaBuilder {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public SwiftSchemaBuilder() {
  }

  @Override
  public ThriftSchema buildSchema(File[] idlFiles) throws SchemaBuilderException {

    final Map<String, Document> documents = new HashMap<>();
    for (File idlFile : idlFiles) {
      logger.trace("Parsing generated IDL: {}", idlFile.getName());
      try {
        final Document document = ThriftIdlParser.parseThriftIdl(
          Files.asCharSource(idlFile, Charsets.UTF_8)
        );
        documents.put(idlFile.getName(), document);
      } catch (IOException e) {
        throw new SchemaBuilderException(e, SCHEMA_103, e.getMessage());
      }
      logger.trace("Parsing {} complete.", idlFile.getName());
    }

    final Document global = documents.get("global.thrift");
    if (global == null) {
      throw new SchemaBuilderException(SCHEMA_100);
    }

    final Builder builder = new Builder().name("ThriftEE");
    for (String include : global.getHeader().getIncludes()) {
      final Document module = documents.get(include);
      if (module == null) {
        throw new SchemaBuilderException(SCHEMA_101, include);
      }
      final String moduleName = includeToModuleName(include);
      SwiftTranslator.translate(builder, moduleName, module);
    }

    return builder.build();
  }
  
  private String includeToModuleName(String include) {
    return include.substring(0, include.length() - 7);
  }
  
}
