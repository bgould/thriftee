/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.swift.parser;

import static com.facebook.swift.parser.Preconditions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.testng.ITestContext;

public class TestingUtils {

  public static Path getResourcePath(String resourceName) {
    try {
      URL rsrc = TestingUtils.class.getClassLoader().getResource(resourceName);
      return Paths.get(rsrc.toURI());
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }

  public static List<Path> listMatchingFiles(Path start, String glob) throws IOException {
    final ArrayList<Path> list = new ArrayList<Path>();
    final PathMatcher matcher = start.getFileSystem().getPathMatcher("glob:" + glob);
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (matcher.matches(file)) {
          list.add(file);
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return Collections.unmodifiableList(list);
  }

  public static String getTestParameter(ITestContext context, String parameterName) {
    String value = context.getCurrentXmlTest().getParameter(parameterName);
    return checkNotNull(value, "test parameter not set: %s", parameterName);
  }

  public static Iterator<Object[]> listDataProvider(Object... list) {
    return listDataProvider(Arrays.asList(list));
  }

  public static Iterator<Object[]> listDataProvider(List<?> list) {
    final List<Object[]> output = new ArrayList<Object[]>(list.size());
    for (Object t : list) {
      output.add(new Object[] { t });
    }
    return output.iterator();
  }

}
