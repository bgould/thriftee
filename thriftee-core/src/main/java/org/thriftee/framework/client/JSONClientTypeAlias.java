package org.thriftee.framework.client;

import java.util.Arrays;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class JSONClientTypeAlias extends ClientTypeAlias {

  public JSONClientTypeAlias() {
    super("json", Generate.JSON, Arrays.asList(new Flag[0]));
  }

}
