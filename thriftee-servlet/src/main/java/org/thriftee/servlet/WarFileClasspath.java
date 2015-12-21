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

import java.lang.ref.WeakReference;
import java.net.URL;

import javax.servlet.ServletContext;

import org.scannotation.WarUrlFinder;
import org.thriftee.framework.Classpath;

public class WarFileClasspath implements Classpath {

  private final WeakReference<ServletContext> reference;
  
  public WarFileClasspath(ServletContext ctx) {
    this.reference = new WeakReference<>(ctx);
  }

  @Override
  public URL[] getUrls() {
    final ServletContext ctx = reference.get();
    if (ctx == null) {
      throw new IllegalStateException("ServletContext seems to have been GC'd");
    }
    final URL[] libs = WarUrlFinder.findWebInfLibClasspaths(ctx);
    final URL classes = WarUrlFinder.findWebInfClassesPath(ctx);
    if (classes == null && libs == null) {
      return new URL[0];
    } else if (classes == null) {
      return libs;
    } else {
      final URL[] result = new URL[libs.length + 1];
      result[0] = classes;
      System.arraycopy(libs, 0, result, 1, libs.length);
      return result;
    }
  }
  
}
