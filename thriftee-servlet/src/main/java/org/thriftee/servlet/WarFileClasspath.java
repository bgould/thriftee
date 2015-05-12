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
