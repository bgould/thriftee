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
import org.thriftee.compiler.schema.SchemaBuilder;
import org.thriftee.compiler.schema.XMLSchemaBuilder;
import org.thriftee.framework.client.ClientTypeAlias;
import org.thriftee.thrift.xml.protocol.TXMLProtocol;
import org.thriftee.util.New;

public class ThriftEEConfig implements Serializable {

  private static final long serialVersionUID = 8148668461656853500L;

  private final File tempDir;

  private final File thriftExecutable;

  private final File thriftLibDir;

  private final SchemaBuilder schemaBuilder;

  private final SchemaProvider schemaProvider;

  private final ServiceLocator serviceLocator;

  private final SortedMap<String, ClientTypeAlias> clientTypeAliases;

  private final SortedMap<String, BaseProtocolTypeAlias> protocolTypeAliases;

  private ThriftEEConfig(
      final File tempDir,
      final File thriftExecutable,
      final File thriftLibDir,
      final SchemaBuilder schemaBuilder,
      final SchemaProvider schemaProvider,
      final ServiceLocator serviceLocator,
      final Map<String, ClientTypeAlias> clientTypeAliases,
      final Map<String, BaseProtocolTypeAlias> protocolTypeAliases) {
    super();
    ensureNotNull("schemaBuilder", schemaBuilder);
    ensureNotNull("schemaProvider", schemaProvider);
    ensureNotNull("serviceLocator", serviceLocator);
    ensureNotNull("clientTypeAliases", clientTypeAliases);
    ensureNotNull("protocolTypeAliases", protocolTypeAliases);
    this.tempDir = tempDir;
    this.thriftExecutable = thriftExecutable;
    this.thriftLibDir = thriftLibDir;
    this.schemaBuilder = schemaBuilder;
    this.schemaProvider = schemaProvider;
    this.serviceLocator = serviceLocator;
    final SortedMap<String, ClientTypeAlias> aliases = New.sortedMap();
    aliases.putAll(clientTypeAliases);
    this.clientTypeAliases = Collections.unmodifiableSortedMap(aliases);
    final SortedMap<String, BaseProtocolTypeAlias> protocols = New.sortedMap();
    protocols.putAll(protocolTypeAliases);
    this.protocolTypeAliases = Collections.unmodifiableSortedMap(protocols);
  }

  private static void ensureNotNull(String name, Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException(name + " cannot be null");
    }
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

  public SortedMap<String, ClientTypeAlias> clientTypeAliases() {
    return this.clientTypeAliases;
  }

  public SortedMap<String, BaseProtocolTypeAlias> protocolTypeAliases() {
    return this.protocolTypeAliases;
  }

  public ServiceLocator serviceLocator() {
    return this.serviceLocator;
  }

  public SchemaBuilder schemaBuilder() {
    return this.schemaBuilder;
  }

  public SchemaProvider schemaProvider() {
    return this.schemaProvider;
  }

  public static class Factory {

    private File tempDir;

    private File thriftExecutable;

    private File thriftLibDir;

    private SchemaBuilder schemaBuilder;

    private SchemaProvider schemaProvider;

    private ServiceLocator serviceLocator;

    private SortedMap<String, ClientTypeAlias> clients = new TreeMap<>();

    private SortedMap<String, BaseProtocolTypeAlias> protocols = new TreeMap<>();

    private boolean useDefaultClientTypeAliases = true;
    
    private boolean useDefaultProtocolTypeAliases = true;

    public void setTempDir(File tempDir) {
      this.tempDir = tempDir;
    }

    public void setThriftExecutable(File file) {
      this.thriftExecutable = file;
    }

    public void setThriftLibDir(File file) {
      this.thriftLibDir = file;
    }

    public void setSchemaBuilder(SchemaBuilder schemaBuilder) {
      this.schemaBuilder = schemaBuilder;
    }

    public void setSchemaProvider(SchemaProvider schemaProvider) {
      this.schemaProvider = schemaProvider;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
      this.serviceLocator = serviceLocator;
    }

    public void setClientTypes(SortedMap<String, ClientTypeAlias> aliases) {
      this.clients = aliases;
    }

    public void setProtocolTypes(SortedMap<String, BaseProtocolTypeAlias> protos) {
      this.protocols = protos;
    }

    public Map<String, ClientTypeAlias> getClientTypes() {
      return clients;
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
        tempDir, 
        thriftExecutable, 
        thriftLibDir, 
        schemaBuilder == null ? new XMLSchemaBuilder() : schemaBuilder,
        schemaProvider,
        serviceLocator,
        clientTypes,
        protocols == null ? new HashMap<String,BaseProtocolTypeAlias>() : protocols
      );
    }

  }

  public static class Builder {
  
    private final Factory factory = new Factory();

    private final SortedMap<String, ClientTypeAlias> aliases = New.sortedMap();

    private final SortedMap<String, BaseProtocolTypeAlias> protocols = New.sortedMap();

    public Builder() {

      addProtocolTypeAlias("binary", new TBinaryProtocol.Factory());
      addProtocolTypeAlias("compact", new TCompactProtocol.Factory());
      addProtocolTypeAlias("json", new TJSONProtocol.Factory());
      addProtocolTypeAlias("tuple", new TTupleProtocol.Factory());
      addProtocolTypeAlias("xml", new TXMLProtocol.Factory());

    }

    public Builder addProtocolTypeAlias(String name, TProtocolFactory factory) {
      final BaseProtocolTypeAlias pta = new BaseProtocolTypeAlias(name, factory);
      addProtocolTypeAlias(pta);
      return this;
    }

    public Builder addProtocolTypeAlias(BaseProtocolTypeAlias protocolType) {
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

    public Builder schemaBuilder(final SchemaBuilder schemaBuilder) {
      factory.setSchemaBuilder(schemaBuilder);
      return this;
    }

    public Builder schemaProvider(final SchemaProvider schemaProvider) {
      factory.setSchemaProvider(schemaProvider);
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
