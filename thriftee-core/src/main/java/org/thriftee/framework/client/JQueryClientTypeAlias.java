package org.thriftee.framework.client;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class JQueryClientTypeAlias extends ClientTypeAlias {

  public JQueryClientTypeAlias() {
    super("jquery", Generate.JS, "js/src", Flag.JS_JQUERY);
  }

}
