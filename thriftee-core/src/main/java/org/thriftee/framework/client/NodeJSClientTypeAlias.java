package org.thriftee.framework.client;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.PostProcessorEvent;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class NodeJSClientTypeAlias extends ClientTypeAlias {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  public NodeJSClientTypeAlias() {
    super("nodejs", Generate.JS, Flag.JS_NODE);
  }

  @Override
  public void postProcess(PostProcessorEvent event) throws IOException {
    
  }

}