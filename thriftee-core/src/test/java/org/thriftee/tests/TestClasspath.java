package org.thriftee.tests;

import java.net.URL;

import org.scannotation.ClasspathUrlFinder;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.examples.classicmodels.services.OrderService;
import org.thriftee.framework.Classpath;

public class TestClasspath implements Classpath {

  @Override
  public URL[] getUrls() {
    URL url1 = ClasspathUrlFinder.findClassBase(OrderService.class);
    URL url2 = ClasspathUrlFinder.findClassBase(ThriftSchema.class);
    return new URL[] { url1, url2, };
  }

}
