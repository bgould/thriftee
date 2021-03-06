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
package org.thriftee.provider.swift.tests;

import java.net.URL;

import org.scannotation.ClasspathUrlFinder;
import org.thriftee.core.Classpath;
import org.thriftee.thrift.schema.ThriftSchema;

public class SwiftTestClasspath implements Classpath {

  @Override
  public URL[] getUrls() {
    URL url1 = ClasspathUrlFinder.findClassBase(CalculatorService.class);
    URL url2 = ClasspathUrlFinder.findClassBase(ThriftSchema.class);
    return new URL[] { url1, url2, };
  }

}
