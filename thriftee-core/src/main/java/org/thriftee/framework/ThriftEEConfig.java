package org.thriftee.framework;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.util.New;

public class ThriftEEConfig implements Serializable {

  private static final long serialVersionUID = 8148668461656853500L;

  private final File tempDir;
  
  private final File thriftExecutable;
  
  private final File thriftLibDir;
  
  private final ScannotationConfigurator scannotationConfigurator;

  private final SortedMap<String, ClientTypeAlias> clientTypeAliases;
  
  private ThriftEEConfig(
      final File tempDir, 
      final File thriftExecutable, 
      final File thriftLibDir, 
      final ScannotationConfigurator configurator,
      final Map<String, ClientTypeAlias> clientTypeAliases) {
    super();
    this.tempDir = tempDir;
    this.thriftExecutable = thriftExecutable;
    this.thriftLibDir = thriftLibDir;
    this.scannotationConfigurator = configurator;
    final SortedMap<String, ClientTypeAlias> aliases = New.sortedMap();
    aliases.putAll(clientTypeAliases);
    this.clientTypeAliases = Collections.unmodifiableSortedMap(aliases);
  }

  public File tempDir() {
    return this.tempDir;
  }
  
  public File thriftExecutable() {
    return this.thriftExecutable;
  }
  
  public File thriftLibDir() {
    return this.thriftLibDir;
  }
  
  public ScannotationConfigurator scannotationConfigurator() {
    return this.scannotationConfigurator;
  }

  public SortedMap<String, ClientTypeAlias> getClientTypeAliases() {
    return this.clientTypeAliases;
  }

  public static class Factory {
    
    private File tempDir;
    
    private File thriftExecutable;
    
    private File thriftLibDir;
    
    private ScannotationConfigurator scannotationConfigurator;
  
    private Map<String, ClientTypeAlias> clientTypeAliases = new HashMap<>();

    public void setTempDir(File tempDir) {
//      if (tempDir == null) {
//        throw new IllegalArgumentException("tempDir cannot be null");
//      }
      this.tempDir = tempDir;
    }
  
    public void setThriftExecutable(File file) {
      this.thriftExecutable = file;
    }
  
    public void setThriftLibDir(File file) {
      this.thriftLibDir = file;
    }
  
    public void setScannotationConfigurator(ScannotationConfigurator configurator) {
      this.scannotationConfigurator = configurator;
    }

    public void setClientTypeAliases(Map<String, ClientTypeAlias> aliases) {
      this.clientTypeAliases = aliases;
    }

    public Map<String, ClientTypeAlias> getClientTypeAliases() {
      return clientTypeAliases;
    }

    public File getTempDir() {
      return tempDir;
    }

    public File getThriftExecutable() {
      return thriftExecutable;
    }

    public File getThriftLibDir() {
      return thriftLibDir;
    }

    public ScannotationConfigurator getScannotationConfigurator() {
      return scannotationConfigurator;
    }
  
    public ThriftEEConfig newInstance() {
      if (tempDir == null) {
        throw new IllegalArgumentException("tempDir cannot be null");
      }
      if (scannotationConfigurator == null) {
        throw new IllegalArgumentException("cannot be null");
      }
      return new ThriftEEConfig(
        tempDir, 
        thriftExecutable, 
        thriftLibDir, 
        scannotationConfigurator,
        clientTypeAliases == null ? Collections.emptyMap() : clientTypeAliases
      );
    }
  
  }
  
  public static class Builder {
  
    private final Factory factory = new Factory();

    private final SortedMap<String, ClientTypeAlias> aliases = New.sortedMap();

    public Builder() {
      addClientTypeAlias("php", Generate.PHP, "php/src", Flag.PHP_NAMESPACE, Flag.PHP_OOP);
      addClientTypeAlias("html", Generate.HTML);
      addClientTypeAlias("json", Generate.JSON);
      addClientTypeAlias("jquery", Generate.JS, Flag.JS_JQUERY);
    }
    
    public Builder addClientTypeAlias(String name, Generate lang, Flag... flags) {
      return addClientTypeAlias(name, lang, null, flags);
    }

    public Builder addClientTypeAlias(
        String name, Generate language, String libDir, Flag... flags) {
      final ClientTypeAlias cta = new ClientTypeAlias(
        name, 
        language,
        libDir,
        Arrays.asList(flags)
      );
      return addClientTypeAlias(cta);
    }

    public Builder addClientTypeAlias(ClientTypeAlias clientTypeAlias) {
      if (aliases.containsKey(clientTypeAlias.getName())) {
        throw new IllegalArgumentException(
          "A type alias named `" + clientTypeAlias.getName() + "` " + 
          "has already been set."
        );
      }
      aliases.put(clientTypeAlias.getName(), clientTypeAlias);
      return this;
    }

    public Builder tempDir(final File tempDir) {
      if (tempDir == null) {
        throw new IllegalArgumentException("tempDir cannot be null");
      }
      factory.setTempDir(tempDir);
      return this;
    }
  
    public Builder thriftExecutable(final File file) {
      factory.setThriftExecutable(file);
      return this;
    }
  
    public Builder thriftLibDir(final File file) {
      factory.setThriftLibDir(file);
      return this;
    }
  
    public Builder scannotationConfigurator(final ScannotationConfigurator configurator) {
      factory.setScannotationConfigurator(configurator);
      return this;
    }
  
    public ThriftEEConfig build() {
      factory.setClientTypeAliases(aliases);
      return factory.newInstance();
    }
  
  }
  
}
