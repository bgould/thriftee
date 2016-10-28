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
package org.thriftee.examples;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.thriftee.servlet.ThriftEEServlet;

import ch.qos.logback.classic.LoggerContext;

@WebServlet(
  urlPatterns = { "/services/*" },
  loadOnStartup = 1,
  name = "Example Service Endpoints"
)
public class ExamplesEndpointServlet extends ThriftEEServlet {

  private static final long serialVersionUID = 920163052774234943L;

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public void init() throws ServletException {
    getServletContext().log("Initializing ThriftEE examples servlet");
    super.init();
    SLF4JBridgeHandler.install();
    LOG.info("Successfully initialized " + getClass().getSimpleName());
  }

  @Override
  public void destroy() {
    try {
      getServletContext().log("uninstalling SLF4JBridgeHandler");
      SLF4JBridgeHandler.uninstall();
    } catch (Throwable e) {
      getServletContext().log("error uninstalling SLF4JBridgeHandler", e);
    }
    try {
      getServletContext().log("Stopping logback context.");
      ((LoggerContext)LoggerFactory.getILoggerFactory()).stop();
    } catch (Throwable e) {
      getServletContext().log("Error stopping logback context.", e);
    }
    super.destroy();
  }

}
