package org.thriftee.framework;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;

public class ClientTypeAlias {

  private final String name;

  private final Generate language;

  private final SortedSet<Flag> flags;
  
  private final String libDir;

  public ClientTypeAlias(String _name, Generate _lang, Iterable<Flag> _flags) {
    this(_name, _lang, null, _flags);
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

}
