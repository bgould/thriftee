package org.thriftee.compiler;

import java.io.File;
import java.io.IOException;

public interface PostProcessor {

  public void postProcess(File dir) throws IOException;

}
