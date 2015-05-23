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
