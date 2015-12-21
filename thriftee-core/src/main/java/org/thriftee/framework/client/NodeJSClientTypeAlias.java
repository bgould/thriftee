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
