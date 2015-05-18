package org.thriftee.framework.client;

import java.util.Arrays;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class HTMLClientTypeAlias extends ClientTypeAlias {

  public HTMLClientTypeAlias() {
    super("html", Generate.HTML, Arrays.asList(new Flag[0]));
  }

}
