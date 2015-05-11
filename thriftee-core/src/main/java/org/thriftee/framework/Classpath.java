package org.thriftee.framework;

import java.io.IOException;
import java.net.URL;

/**
 * Implementors of this class are responsible for configuring an
 * {@link org.scannotation.AnnotationDB} in order to search for runtime
 * annotations.
 * 
 * @author bcg 
 */
public interface Classpath {

  /**
   * Configures and instance of {@link org.scannotation.AnnotationDB} with classpaths to search.
   * 
   * @param db The database to configure
   * @throws IOException
   */
  public URL[] getUrls();

}
