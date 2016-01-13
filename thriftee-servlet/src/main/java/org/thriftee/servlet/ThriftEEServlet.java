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
package org.thriftee.servlet;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.ext.servlet.ServerServlet;
import org.thriftee.framework.ServiceLocator;
import org.thriftee.framework.ThriftEE;
import org.thriftee.framework.ThriftEEConfig;
import org.thriftee.framework.ThriftEEFactory;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.provider.swift.SwiftSchemaBuilder;
import org.thriftee.provider.swift.SwiftSchemaProvider;
import org.thriftee.restlet.FrameworkResource;
import org.thriftee.restlet.ThriftApplication;

/*
 * @author bcg
 */
public class ThriftEEServlet extends ServerServlet {

  public static final String THRIFT_EXECUTABLE_PARAM = "thrift.executable";

  public static final String THRIFT_LIB_DIR_PARAM = "thrift.lib.dir";

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      initializeThrift();
    } catch (ThriftStartupException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy() {
    if (_thriftee != null) {
      try {
        _thriftee.destroy();
      } catch (Exception e) {
        System.err.println("Error while destroying ThriftEE context:");
        e.printStackTrace();
      }
    }
    super.destroy();
  }

  @Override
  protected Application createApplication(Context parentContext) {
    return new ThriftApplication(parentContext.createChildContext());
  }

  @Override
  protected void init(Component component) {
    super.init(component);
    FrameworkResource.initComponent(component);
  }

  @Override
  protected void init(Application app) {
    super.init(app);
    FrameworkResource.initApplication(app, thriftee());
  }

  protected synchronized ThriftEE thriftee() {
    if (_thriftee == null) {
      throw new IllegalStateException("ThriftEE has not been initialized.");
    }
    return _thriftee;
  }

  protected File readThriftExecutable() {
    String executable = getInitParameter(THRIFT_EXECUTABLE_PARAM);
    if (executable == null) {
      executable = getServletContext().getInitParameter(THRIFT_EXECUTABLE_PARAM);
      if (executable == null) {
        executable = System.getProperty(THRIFT_EXECUTABLE_PARAM);
      }
    }
    if (executable != null) {
      return new File(executable);
    }
    return new File("/usr/local/src/bin/thrift");
  }

  protected File readThriftLibDir() {
    String libDir = getInitParameter(THRIFT_LIB_DIR_PARAM);
    if (libDir == null) {
      libDir = getServletContext().getInitParameter(THRIFT_LIB_DIR_PARAM);
      if (libDir == null) {
        libDir = System.getProperty(THRIFT_LIB_DIR_PARAM);
      }
    }
    if (libDir != null) {
      return new File(libDir);
    }
    return new File("/usr/local/src/thrift/lib");
  }

  protected ServiceLocator createServiceLocator() {
    final DefaultEJBServiceLocator locator = new DefaultEJBServiceLocator();
    locator.setSearchAllModules(true);
    return locator;
  }
 
  private static final long serialVersionUID = 9217322620918070877L;

  private volatile ThriftEE _thriftee;
  
  private synchronized void initializeThrift() throws ThriftStartupException {
    if (_thriftee != null) {
      throw new IllegalStateException(
        "ThriftEE is already initialized for this servlet.");
    }
    final ServletContext ctx = getServletContext();
    final File tempDir = (File) ctx.getAttribute(ServletContext.TEMPDIR);
    final File thriftExecutable = readThriftExecutable();
    final File thriftLibDir = readThriftLibDir();
    final ServiceLocator serviceLocator = createServiceLocator();
    final ThriftEEConfig.Builder builder = new ThriftEEConfig.Builder()
      .tempDir(tempDir)
      .thriftExecutable(thriftExecutable)
      .thriftLibDir(thriftLibDir)
      .serviceLocator(serviceLocator)
      .schemaProvider(new SwiftSchemaProvider(true, new WarFileClasspath(ctx)))
      .schemaBuilder(new SwiftSchemaBuilder());
    initConfigBuilder(builder);
    final ThriftEEConfig config = builder.build();
    final ThriftEE svcs = new ThriftEEFactory().create(config);
    this._thriftee = svcs;
  }

  /**
   * Subclasses should override this method to add/update configuration options
   * @param builder
   */
  protected void initConfigBuilder(ThriftEEConfig.Builder builder) {
  }

}
