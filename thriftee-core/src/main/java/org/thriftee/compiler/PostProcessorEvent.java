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
package org.thriftee.compiler;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class PostProcessorEvent {

  private final File directory;

  private final File thriftLibDir;

  private final List<File> extraDirs;

  public PostProcessorEvent(
      final File dir, 
      final File thriftLibDir,
      final List<File> extraDirs) {
    this.directory = dir;
    this.thriftLibDir = thriftLibDir;
    if (extraDirs != null) {
      this.extraDirs = Collections.unmodifiableList(extraDirs);
    } else {
      this.extraDirs = null;
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  public File getThriftLibDir() {
    return this.thriftLibDir;
  }

  public List<File> getExtraDirs() {
    return this.extraDirs;
  }

}
