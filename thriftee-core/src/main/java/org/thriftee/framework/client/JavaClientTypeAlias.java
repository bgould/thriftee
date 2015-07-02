package org.thriftee.framework.client;

import java.util.Arrays;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class JavaClientTypeAlias extends ClientTypeAlias {

  public JavaClientTypeAlias() {
    super("java", Generate.JAVA, Arrays.asList(new Flag[0]));
  }

}
