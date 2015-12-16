package org.thriftee.framework;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TTupleProtocol;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommand.Generate.Flag;
import org.thriftee.framework.client.ClientTypeAlias;
import org.thriftee.thrift.xml.protocol.TXMLProtocol;
import org.thriftee.util.New;

public class ThriftEEConfig implements Serializable {

  private static final long serialVersionUID = 8148668461656853500L;

  private final boolean useBytecodeCompiler;

  private final File tempDir;

  private final File thriftExecutable;

  private final File thriftLibDir;

  private final Classpath annotationClasspath;

  private final ServiceLocator serviceLocator;

  private final SortedMap<String, ClientTypeAlias> clientTypeAliases;

  private final SortedMap<String, ProtocolTypeAlias> protocolTypeAliases;

  private ThriftEEConfig(
      final boolean useBytecodeCompiler,
      final File tempDir,
      final File thriftExecutable,
      final File thriftLibDir,
      final Classpath annotationClasspath,
      final ServiceLocator serviceLocator,
      final Map<String, ClientTypeAlias> clientTypeAliases,
      final Map<String, ProtocolTypeAlias> protocolTypeAliases) {
    super();
    this.useBytecodeCompiler = useBytecodeCompiler;
    this.tempDir = tempDir;
    this.thriftExecutable = thriftExecutable;
    this.thriftLibDir = thriftLibDir;
    this.annotationClasspath = annotationClasspath;
    this.serviceLocator = serviceLocator;
    final SortedMap<String, ClientTypeAlias> aliases = New.sortedMap();
    aliases.putAll(clientTypeAliases);
    this.clientTypeAliases = Collections.unmodifiableSortedMap(aliases);
    final SortedMap<String, ProtocolTypeAlias> protocols = New.sortedMap();
    protocols.putAll(protocolTypeAliases);
    this.protocolTypeAliases = Collections.unmodifiableSortedMap(protocols);
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

  public Classpath annotationClasspath() {
    return this.annotationClasspath;
  }

  public SortedMap<String, ClientTypeAlias> clientTypeAliases() {
    return this.clientTypeAliases;
  }

  public SortedMap<String, ProtocolTypeAlias> protocolTypeAliases() {
    return this.protocolTypeAliases;
  }

  public ServiceLocator serviceLocator() {
    return this.serviceLocator;
  }

  public boolean useBytecodeCompiler() {
    return this.useBytecodeCompiler;
  }

  public static class Factory {

    private boolean useBytecodeCompiler = true;

    private File tempDir;

    private File thriftExecutable;

    private File thriftLibDir;

    private Classpath annotationClasspath;

    private ServiceLocator serviceLocator;

    private SortedMap<String, ClientTypeAlias> clients = new TreeMap<>();

    private SortedMap<String, ProtocolTypeAlias> protocols = new TreeMap<>();

    private boolean useDefaultClientTypeAliases = true;
    
    private boolean useDefaultProtocolTypeAliases = true;

    public void setUseBytecodeCompiler(boolean useBytecodeCompiler) {
      this.useBytecodeCompiler = useBytecodeCompiler;
    }

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

    public void setAnnotationClasspath(Classpath classpath) {
      this.annotationClasspath = classpath;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
      this.serviceLocator = serviceLocator;
    }

    public void setClientTypes(SortedMap<String, ClientTypeAlias> aliases) {
      this.clients = aliases;
    }

    public void setProtocolTypes(SortedMap<String, ProtocolTypeAlias> protos) {
      this.protocols = protos;
    }

    public Map<String, ClientTypeAlias> getClientTypes() {
      return clients;
    }

    public boolean isUseBytecodeCompiler() {
      return useBytecodeCompiler;
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

    public Classpath getAnnotationClasspath() {
      return annotationClasspath;
    }

    public boolean isUseDefaultClientTypeAliases() {
      return useDefaultClientTypeAliases;
    }

    public void setUseDefaultClientTypeAliases(boolean useDefaults) {
      this.useDefaultClientTypeAliases = useDefaults;
    }

    public boolean isUseDefaultProtocolTypes() {
      return useDefaultProtocolTypeAliases;
    }

    public void setUseDefaultProtocolTypes(boolean useDefaults) {
      this.useDefaultProtocolTypeAliases = useDefaults;
    }

    public ThriftEEConfig newInstance() {
      if (tempDir == null) {
        throw new IllegalArgumentException("tempDir cannot be null");
      }
      if (annotationClasspath == null) {
        throw new IllegalArgumentException("cannot be null");
      }
      final Map<String, ClientTypeAlias> clientTypes = new TreeMap<>();
      if (isUseDefaultClientTypeAliases()) {
        for (ClientTypeAlias.Defaults def : ClientTypeAlias.Defaults.values()) {
          final ClientTypeAlias cta = def.getInstance();
          clientTypes.put(cta.getName(), cta);
        }
      }
      if (clients != null) {
        for (final String key : clients.keySet()) {
          final ClientTypeAlias cta = clients.get(key);
          clientTypes.put(key, cta);
        }
      }
      return new ThriftEEConfig(
        useBytecodeCompiler,
        tempDir, 
        thriftExecutable, 
        thriftLibDir, 
        annotationClasspath,
        serviceLocator,
        clientTypes,
        protocols == null ? new HashMap<String,ProtocolTypeAlias>() : protocols
      );
    }

  }

  public static class Builder {
  
    private final Factory factory = new Factory();

    private final SortedMap<String, ClientTypeAlias> aliases = New.sortedMap();

    private final SortedMap<String, ProtocolTypeAlias> protocols = New.sortedMap();

    public Builder() {

      addProtocolTypeAlias("binary", new TBinaryProtocol.Factory());
      addProtocolTypeAlias("compact", new TCompactProtocol.Factory());
      addProtocolTypeAlias("json", new TJSONProtocol.Factory());
      addProtocolTypeAlias("tuple", new TTupleProtocol.Factory());
      addProtocolTypeAlias("xml", new TXMLProtocol.Factory());

    }

    public Builder useBytecodeCompiler(boolean useBytecodeCompiler) {
      this.factory.setUseBytecodeCompiler(useBytecodeCompiler);
      return this;
    }

    public Builder addProtocolTypeAlias(String name, TProtocolFactory factory) {
      final ProtocolTypeAlias pta = new ProtocolTypeAlias(name, factory);
      addProtocolTypeAlias(pta);
      return this;
    }

    public Builder addProtocolTypeAlias(ProtocolTypeAlias protocolType) {
      final String name = protocolType.getName();
      if (protocols.containsKey(name)) {
        throw new IllegalArgumentException(
          "A type alias named `" + name + "` has already been set."
        );
      }
      protocols.put(name, protocolType);
      return this;
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

    public Builder annotationClasspath(final Classpath classpath) {
      factory.setAnnotationClasspath(classpath);
      return this;
    }

    public Builder serviceLocator(final ServiceLocator serviceLocator) {
      factory.setServiceLocator(serviceLocator);
      return this;
    }

    public ThriftEEConfig build() {
      factory.setClientTypes(aliases);
      factory.setProtocolTypes(protocols);
      return factory.newInstance();
    }

  }

}
