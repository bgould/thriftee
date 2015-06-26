package org.thriftee.framework.client;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class PHPClientTypeAlias extends ClientTypeAlias {

  public PHPClientTypeAlias() {
    super("php", Generate.PHP, "php/lib", Flag.PHP_NAMESPACE, Flag.PHP_OOP);
  }

}
