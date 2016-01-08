package org.thriftee.framework;

import java.io.File;

public interface SchemaBuilderConfig {

  File thriftLibDir();

  File thriftExecutable();

  String thriftVersionString();

  File tempDir();

  File idlDir();

  File[] idlFiles();

  File globalIdlFile();

  File globalXmlFile();

}