package org.thriftee.provider.swift;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.schema.SchemaBuilder;
import org.thriftee.compiler.schema.SchemaBuilderException;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.compiler.schema.ThriftSchema.Builder;
import org.thriftee.framework.ThriftEE;
import org.thriftee.util.New;

import com.facebook.swift.parser.ThriftIdlParser;
import com.facebook.swift.parser.model.Document;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import static org.thriftee.compiler.schema.SchemaBuilderException.Messages.*;

public class SwiftSchemaBuilder implements SchemaBuilder {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  public SwiftSchemaBuilder() {
  }
  
  @Override
  public ThriftSchema buildSchema(ThriftEE thrift) throws SchemaBuilderException {
    
    final Charset cs = Charset.forName("UTF-8");
    final Map<String, Document> documents = New.map();
    for (File idlFile : thrift.idlFiles()) {
      logger.trace("Parsing generated IDL: {}", idlFile.getName());
      try {
        final CharSource input = Files.asCharSource(idlFile, cs);
        final Document document = ThriftIdlParser.parseThriftIdl(input);
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
