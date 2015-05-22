package org.thriftee.framework.client;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.thriftee.compiler.PostProcessor;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class ClientTypeAlias implements PostProcessor {

  private final String name;

  private final Generate language;

  private final SortedSet<Flag> flags;
  
  private final String libDir;

  public ClientTypeAlias(String _name, Generate _lang, Iterable<Flag> _flags) {
    this(_name, _lang, null, _flags);
  }

  public ClientTypeAlias(String _name, Generate _lang, Flag... _flags) {
    this(_name, _lang, null, Arrays.asList(_flags != null ? _flags : new Flag[0]));
  }

  public ClientTypeAlias(String _name, Generate _lang, String libDir, Flag... _flags) {
    this(_name, _lang, libDir, Arrays.asList(_flags != null ? _flags : new Flag[0]));
  }

  public ClientTypeAlias(String _name, Generate _lang, String libDir, Iterable<Flag> _flags) {
    // TODO: see if these exceptions should be converted to proper heirarchy
    if (_name == null) {
      throw new IllegalArgumentException("name is required.");
    }
    if (_lang == null) {
      throw new IllegalArgumentException("language is required.");
    }
    if (_flags == null) {
      throw new IllegalArgumentException("flag set cannot be null.");
    }
    this.name = _name;
    this.language = _lang;
    final SortedSet<Flag> flagSet = new TreeSet<>();
    for (final Flag flag : _flags) {
      flagSet.add(flag);
    }
    this.flags = Collections.unmodifiableSortedSet(flagSet);
    this.libDir = libDir;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the language
   */
  public Generate getLanguage() {
    return language;
  }

  /**
   * @return the flags
   */
  public Set<Flag> getFlags() {
    return flags;
  }

  /**
   * @return the libDir
   */
  public String getLibDir() {
    return libDir;
  }

  /**
   * Subclasses may override this to post-process generated files.
   * @param dir Location where the generated file are located
   */
  public void postProcess(File dir) throws IOException {}

  public static enum Defaults {

    HTML(new HTMLClientTypeAlias()),
    JQUERY(new JQueryClientTypeAlias()),
    JSON(new JSONClientTypeAlias()),
    NODE(new NodeJSClientTypeAlias()),
    PHP(new PHPClientTypeAlias()),
    ;

    private ClientTypeAlias instance;

    private Defaults(ClientTypeAlias alias) {
      this.instance = alias;
    }

    public ClientTypeAlias getInstance() {
      return this.instance;
    }

  }

}
