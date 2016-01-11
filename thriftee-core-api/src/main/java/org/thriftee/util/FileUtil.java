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
package org.thriftee.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

  public static final Charset UTF_8 = Charset.forName("UTF-8");

  public static String readAsString(File file, String encoding) throws IOException {
    InputStream in = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream((int) file.length());
    try {
      in = new FileInputStream(file);
      if (((int) file.length()) > 0) {
        byte[] buffer = new byte[1024];
        for (int n = -1; (n = in.read(buffer)) > -1; ) {
          baos.write(buffer, 0, n);
        }
      }
    } finally {
      forceClosed(in);
    }
    return baos.toString(encoding);
  }

  public static void writeStringToFile(String s, File file, Charset encoding) 
      throws IOException {
    try (final FileOutputStream out = new FileOutputStream(file)) {
      try (final Writer w = new OutputStreamWriter(out, encoding)) {
        w.write(s);
      }
    }
  }

  public static void createZipFromDirectory(
      File outputFile, 
      String zipBaseDir, 
      File fileBaseDir, 
      File... extraZipDirectories) throws IOException {
    FileOutputStream os = null;
    ZipOutputStream zip = null;
    try {
      os = new FileOutputStream(outputFile);
      zip = new ZipOutputStream(os);
      addDirectory(zip, fileBaseDir, zipBaseDir);
      if (extraZipDirectories != null) {
        for (File extraDir : extraZipDirectories) {
          addDirectory(zip, extraDir, "");
        }
      }
    } finally {
      forceClosed(zip);
      forceClosed(os);
    }
  }
  
  private static void addDirectory(ZipOutputStream zip, File source, String basePath) throws IOException {
    final File[] files = source.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (file.isDirectory()) {
          String path = basePath + file.getName() + "/";
          zip.putNextEntry(new ZipEntry(path));
          addDirectory(zip, file, path);
          zip.closeEntry();
        } else {
          FileInputStream fileIn = null;
          try {
            fileIn = new FileInputStream(file);
            String path = basePath + file.getName();
            zip.putNextEntry(new ZipEntry(path));
            copy(fileIn, zip);
            zip.closeEntry();
          } finally {
            forceClosed(fileIn);
          }
        }
      }
    }
  }
  
  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    for (int n; (n = in.read(buffer)) > -1; ) {
      out.write(buffer, 0, n);
    }
  }

  public static void copyFile(File src, File dest) throws IOException {
    if (src == null) {
      throw new IOException("src cannot be null");
    }
    if (src.isDirectory()) {
      throw new IOException("cannot be a directory: " + src.getAbsolutePath());
    }
    copyRecursively(src, dest);
  }
  
  public static void copyRecursively(File src, File dest) throws IOException {
    if (src.isDirectory()) {
      final File[] files = src.listFiles();
      if (files != null) {
        for (final File c : files) {
          if (c.isDirectory()) {
            final File destdir = new File(dest, c.getName());
            if (!destdir.exists() && !destdir.mkdir()) {
              throw new IOException(
                "could not create directory: " + 
                destdir.getAbsolutePath()
              );
            }
            copyRecursively(c, destdir);
          } else {
            File destfile = new File(dest, c.getName());
            copyRecursively(c, destfile);
          }
        }
      }
    } else {
      FileInputStream in = null;
      FileOutputStream out = null;
      try {
        in = new FileInputStream(src);
        out = new FileOutputStream(dest);
        copy(in, out);
      } finally {
        if (in != null) { try { in.close(); } catch (Exception e) {} }
        if (out != null) { try { out.close(); } catch (Exception e) {} }
      }
    }
  }

  public static void deleteRecursively(File file) throws IOException {
    if (file.isDirectory()) {
      final File[] files = file.listFiles();
      if (files != null) {
        for (File c : files) {
          deleteRecursively(c);
        }
      }
    }
    if (!file.delete()) {
      throw new FileNotFoundException("Failed to delete file: " + file);
    }
  }
  
  public static void forceClosed(InputStream stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {}
    }
  }
  
  public static void forceClosed(OutputStream stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {}
    }
  }

  public static int urlToFile(URL url, File file) throws IOException {
    InputStream in = null;
    FileOutputStream out = null;
    try {
      in = url.openStream();
      out = new FileOutputStream(file);
      final byte[] buffer = new byte[1024];
      int bytesRead = 0;
      for (int n; ((n = in.read(buffer)) > -1); bytesRead += n) {
        out.write(buffer, 0, n);
      }
      return bytesRead;
    } finally {
      forceClosed(in);
      forceClosed(out);
    }
  }

  public static Properties readProperties(InputStream in) throws IOException {
    Properties props = new Properties();
    props.load(in);
    return props;
  }
  
  public static Properties readProperties(File file) throws IOException {
    FileInputStream in = null;
    try {
      in = new FileInputStream(file);
      Properties props = new Properties();
      props.load(in);
      return props;
    } finally {
      forceClosed(in);
    }
  }

  public static Properties readProperties(URL url) throws IOException {
    InputStream in = null;
    try {
      in = url.openStream();
      Properties props = new Properties();
      props.load(in);
      return props;
    } finally {
      forceClosed(in);
    }
  }
  
  public static void writeProperties(File file, Properties props) throws IOException {
    writeProperties(file, props, null);
  }
  
  public static void writeProperties(File file, Properties props, String comments) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      props.store(out, comments);
    } finally {
      forceClosed(out);
    }
  }
}
