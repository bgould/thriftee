package org.thriftee.framework;

import java.io.File;
import java.util.SortedMap;

import org.apache.thrift.TProcessor;

public interface SchemaProvider {

  public File[] exportIdl(File idlDir) throws ThriftStartupException;

  public SortedMap<String, TProcessor> buildProcessorMap(
      ServiceLocator serviceLocator) throws ThriftStartupException;

}
