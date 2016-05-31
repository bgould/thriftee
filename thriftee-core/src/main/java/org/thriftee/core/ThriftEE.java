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
package org.thriftee.core;

import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_000;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_003;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_004;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_008;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_011;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_012;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_013;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_014;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_015;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.schema.SchemaBuilderException;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.core.ThriftStartupException.ThriftStartupMessage;
import org.thriftee.core.client.ClientTypeAlias;
import org.thriftee.core.compiler.ProcessIDL;
import org.thriftee.core.compiler.ThriftCommand;
import org.thriftee.core.compiler.ThriftCommand.Generate;
import org.thriftee.core.compiler.ThriftCommandException;
import org.thriftee.core.compiler.ThriftCommandRunner;
import org.thriftee.core.service.ThriftSchemaServiceImpl;
import org.thriftee.core.util.FileUtil;
import org.thriftee.meta.idl.ThriftSchemaService;
import org.thriftee.thrift.compiler.ExecutionResult;
import org.thriftee.thrift.compiler.ThriftCompiler;
import org.thriftee.thrift.xml.Transforms;
import org.thriftee.thrift.xml.protocol.TXMLProtocol;

public final class ThriftEE implements SchemaBuilderConfig {

  public static final Charset XML_CHARSET = Charset.forName("UTF-8");

  public static final String MODULE_NAME_META = "org.thriftee.meta";

  public static final String MODULE_NAME_META_IDL = "org.thriftee.meta.idl";

  public static final String MODULE_NAME_COMPILER_IDL = "org.thriftee.compiler.idl";

  public ServiceLocator serviceLocator() {
    return serviceLocator;
  }

  public SortedMap<String, ClientTypeAlias> clientTypeAliases() {
    return clientTypeAliases;
  }

  public SortedMap<String, BaseProtocolTypeAlias> protocolTypeAliases() {
    return protocolTypeAliases;
  }

  @Override
  public File thriftLibDir() {
    return this.thriftLibDir;
  }

  @Override
  public ThriftCompiler thriftCompiler() {
    return this.compiler;
  }

  @Override
  public String thriftVersionString() {
    return this.thriftVersionString;
  }

  @Override
  public File tempDir() {
    return this.tempDir;
  }

  public File clientsDir() {
    return this.clientsDir;
  }

  public File wsdlClientDir() {
    return this.wsdlClientDir;
  }

  @Override
  public File idlDir() {
    return this.idlDir;
  }

  @Override
  public File[] idlFiles() {
    File[] newArr = new File[idlFiles == null ? 0 : idlFiles.length];
    if (newArr.length > 0) {
      System.arraycopy(idlFiles, 0, newArr, 0, idlFiles.length);
    }
    return newArr;
  }

  public File clientLibraryDir(final String name) {
    final String prefix = validateLibrary(name);
    final File dir = new File(clientsDir(), prefix);
    if (!dir.exists() || !dir.isDirectory()) {
      throw new IllegalStateException(
        "client dir does not exist: " + dir.getAbsolutePath());
    }
    return dir;
  }

  public File clientLibraryZip(final String name) {
    final String prefix = clientLibraryPrefix(name);
    final File zip = new File(clientsDir(), prefix + ".zip");
    if (!zip.exists() || !zip.isFile()) {
      throw new IllegalStateException(
        "client zip file does not exist: " + zip.getAbsolutePath());
    }
    return zip;
  }

  public ThriftSchema schema() {
    return this.schema;
  }

  @Override
  public File globalIdlFile() {
    return this.globalIdlFile;
  }

  @Override
  public File globalXmlFile() {
    return this.globalXmlFile;
  }

  public Transforms xmlTransforms() {
    return this.transforms;
  }

  public TMultiplexedProcessor multiplexedProcessor() {
    LOG.trace("Building multiplexed processor");
    final TMultiplexedProcessor multiplex = new TMultiplexedProcessor();
    for (Entry<String, TProcessor> entry : this.processors.entrySet()) {
      if (entry.getKey() == null) {
        throw new IllegalStateException("One or more processors was null.");
      }
      LOG.trace("registering with multiplex processor: {}", entry.getKey());
      multiplex.registerProcessor(entry.getKey(), entry.getValue());
    }
    return multiplex;
  }

  public TProcessor processorFor(final ServiceSchema svc) {
    final String serviceName = svc.getModule().getName() + "." + svc.getName();
    final TProcessor processor = this.processors.get(serviceName);
    if (processor == null) {
      throw new IllegalArgumentException("processor not found for svc: " + svc);
    }
    return processor;
  }

  public void destroy() {
    if (transforms != null) {
      transforms.release();
    }
  }

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  private final ThriftCompiler compiler;

  private final File tempDir;

  private final File clientsDir;

  private final File wsdlClientDir;

  private final File idlDir;

  private final File thriftLibDir;

  private final String thriftVersionString;

  private final File[] idlFiles;

  private final File globalIdlFile;

  private final File globalXmlFile;

  private final SortedMap<String, ClientTypeAlias> clientTypeAliases;

  private final SortedMap<String, BaseProtocolTypeAlias> protocolTypeAliases;

  private final SortedMap<String, TProcessor> processors;

  private final ServiceLocator serviceLocator;

  private final ThriftSchema schema;

  private final Transforms transforms;

  public ThriftEE(final ThriftEEConfig config) throws ThriftStartupException {

    this.transforms = new Transforms();
    this.tempDir = new File(config.tempDir(), "thriftee");
    this.clientsDir = new File(this.tempDir, "clients");
    this.wsdlClientDir = new File(this.clientsDir, "wsdl");
    this.idlDir = new File(tempDir, "idl");
    if (config.serviceLocator() != null) {
      this.serviceLocator = config.serviceLocator();
    } else {
      this.serviceLocator = new DefaultServiceLocator();
    }

    if (config.clientTypeAliases() == null) {
      this.clientTypeAliases = new TreeMap<>();
    } else {
      this.clientTypeAliases = config.clientTypeAliases();
    }

    if (config.protocolTypeAliases() == null) {
      this.protocolTypeAliases = new TreeMap<>();
    } else {
      this.protocolTypeAliases = config.protocolTypeAliases();
    }

    this.thriftLibDir = unzipLibraries();
    LOG.info("Thrift library dir: {}", thriftLibDir);

    //------------------------------------------------------------------//
    // Next we will validate the thrift executable and make note of the //
    // version that it returns when called.                             //
    //------------------------------------------------------------------//
    this.compiler = ThriftCompiler.newCompiler();
    try {
      this.thriftVersionString = getVersionString();
    } catch (ThriftCommandException e) {
      throw new ThriftStartupException(e, STARTUP_008, e.getMessage());
    }
    LOG.info("Thrift compiler implementation: {}", this.compiler);
    LOG.info("Thrift version string: {}", this.thriftVersionString);

    //------------------------------------------------------------------//
    // Generate a global IDL file that includes all other IDL files     //
    //------------------------------------------------------------------//
    final File[] idlFiles = config.schemaProvider().exportIdl(idlDir());
    this.idlFiles = createGlobalIdlFile(idlFiles);
    this.globalIdlFile = this.idlFiles[0];

    //------------------------------------------------------------------//
    // Export global IDL file as XML for building the schema model      //
    //------------------------------------------------------------------//
    LOG.debug("Exporting XML definitions from IDL files");
    final File globalXml = new File(idlDir(), MODULE_NAME_META + ".xml");
    final boolean xmlSuccessful = generateGlobalXml(globalXml);
    if (!xmlSuccessful) {
      throw new ThriftStartupException(STARTUP_011, "unknown error");
    }
    final String xmlValidationError;
    try {
      final URL xsdurl = transforms.schemaUrl();
      final StreamSource source = new StreamSource(globalXml);
      xmlValidationError = TXMLProtocol.XML.validate(xsdurl, source);
    } catch (Exception e) {
      throw new ThriftStartupException(e, STARTUP_012, e.getMessage());
    }
    if (xmlValidationError != null) {
      throw new ThriftStartupException(STARTUP_013, xmlValidationError);
    }
    this.globalXmlFile = globalXml;

    //------------------------------------------------------------------//
    // At this point we will parse the generated IDL and store the meta //
    // model of the schema. Loosely typed clients or clients incapable  //
    // of introspection can use the meta model as a sort of reflection. //
    //------------------------------------------------------------------//
    try {
      final SchemaBuilder schemaBuilder = config.schemaBuilder();
      this.schema = schemaBuilder.buildSchema(this);
    } catch (SchemaBuilderException e) {
      throw new ThriftStartupException(e, STARTUP_003, e.getMessage());
    }

    //------------------------------------------------------------------//
    // Generate the XML artifacts for thrift-to-SOAP conversion         //
    // TODO: refactor XML to be "just another client"                   //
    //------------------------------------------------------------------//
    this.wsdlClientDir.mkdirs();
    try {
      transforms.preload(globalXmlFile);
      transforms.exportSchemas(globalXmlFile, this.wsdlClientDir);
      transforms.exportWsdls(globalXmlFile, this.wsdlClientDir);
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_014, e.getMessage());
    }

    //------------------------------------------------------------------//
    // Export the client libraries                                      //
    //------------------------------------------------------------------//
    LOG.debug("Exporting configured clients");
    for (final ClientTypeAlias alias : clientTypeAliases().values()) {
      generateClientLibrary(alias);
    }

    //------------------------------------------------------------------//
    // Register the thrift processors                                   //
    //------------------------------------------------------------------//
    LOG.debug("Setting up thrift processor map");
    final SortedMap<String, TProcessor> processorMap;
    try {
      final String svcname = MODULE_NAME_META_IDL + ".ThriftSchemaService";
      final ThriftSchemaService.Iface svc = new ThriftSchemaServiceImpl(this);
      processorMap = config.schemaProvider().buildProcessorMap(serviceLocator);
      processorMap.put(svcname, new ThriftSchemaService.Processor<>(svc));
    } catch (SchemaBuilderException e) {
      throw new ThriftStartupException(e, STARTUP_000);
    }
    this.processors = Collections.unmodifiableSortedMap(processorMap);

    LOG.info("Thrift initialization completed");
  }

  public static boolean validateThriftLibraryDir(File thriftLibDir) {
    if (thriftLibDir == null) {
      return false;
    } else {
      return new File(thriftLibDir, "php/lib/Thrift").exists();
    }
  }

  private ThriftCommandRunner newCommandRunner() {
    final ThriftCommand command = new ThriftCommand((Generate) null);
    return ThriftCommandRunner.instanceFor(compiler, command);
  }

  public final String getVersionString() {
    return newCommandRunner().executeVersion();
  }

  public final  String getHelpString() {
    return newCommandRunner().executeHelp();
  }

  private String validateLibrary(String name) {
    if (!clientTypeAliases().containsKey(name)) {
      throw new IllegalArgumentException("Invalid client type alias name");
    }
    return name;
  }

  private String clientLibraryPrefix(String name) {
    return "client-" + validateLibrary(name);
  }

  private File[] createGlobalIdlFile(final File[] idlFiles)
      throws ThriftStartupException {
    final File outdir = new File(idlDir(), "thrift");
    if (outdir.exists() && !outdir.isDirectory()) {
      throw new ThriftStartupException(STARTUP_004,
        outdir.getAbsolutePath() + " exists and is not a directory.");
    } else if (!outdir.exists() && !outdir.mkdirs()) {
      throw new ThriftStartupException(STARTUP_004,
        outdir.getAbsolutePath() + " does not exist and could not be created.");
    }
    final String meta = MODULE_NAME_META + ".thrift";
    final String metaIdl = MODULE_NAME_META_IDL + ".thrift";
    final String compilerIdl = MODULE_NAME_COMPILER_IDL + ".thrift";
    try {
      final File globalFile = new File(outdir, meta);
      final File metaIdlFile = FileUtil.copyResourceToDir(metaIdl, outdir);
      final File compilerIdlFile = FileUtil.copyResourceToDir(compilerIdl, outdir);
      final List<File> allFiles = new ArrayList<>(idlFiles.length + 3);
      allFiles.add(globalFile);
      allFiles.add(metaIdlFile);
      allFiles.add(compilerIdlFile);
      final StringBuilder includes = new StringBuilder();
      includes.append("include \"" + metaIdlFile.getName() + "\"\n");
      includes.append("include \"" + compilerIdlFile.getName() + "\"\n");
      for (File idlFile : idlFiles) {
        includes.append("include \"" + idlFile.getName() + "\"\n");
        allFiles.add(idlFile);
      }
      FileUtil.writeStringToFile(includes.toString(), globalFile, FileUtil.UTF_8);
      return allFiles.toArray(new File[allFiles.size()]);
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_004, "IOException occurred");
    }
  }

  private boolean generateGlobalXml(File out) throws ThriftStartupException {
    final File xmlDir = out.getParentFile();
    if (!xmlDir.exists()) {
      if (!xmlDir.mkdirs()) {
        throw new ThriftStartupException(STARTUP_011, String.format(
          "could not create directory for XML model output: %s",
            xmlDir.getAbsolutePath()));
      }
    }
    final String path = globalIdlFile().getAbsolutePath();
    final ThriftCommand cmd = new ThriftCommand(Generate.XML, path);
    cmd.setOutputLocation(xmlDir);
    cmd.addFlag(Generate.Flag.XML_MERGE);
    final ThriftCommandRunner runner =
        ThriftCommandRunner.instanceFor(compiler, cmd);
    final ExecutionResult result = runner.executeCommand();
    return result.successful();
  }

  private void generateClientLibrary(ClientTypeAlias alias)
      throws ThriftStartupException {
    final String name = alias.getName();
    LOG.debug("Generating library for client type alias: {}", name);
    try {
      final ThriftCommand cmd = new ThriftCommand(alias);
      cmd.setRecurse(true);
      final File[] extraDirs;
      if (thriftLibDir() != null && alias.getLibDir() != null) {
        final File libDir = new File(thriftLibDir(), alias.getLibDir());
        extraDirs = new File[] { libDir };
      } else {
        extraDirs = new File[0];
      }
      final File[] files = new File[] { globalIdlFile() };
      final ProcessIDL idlProcessor = new ProcessIDL(
        thriftCompiler(),
        thriftLibDir(),
        alias
      );
      final String zipName = clientLibraryPrefix(name);
      final File clientLibrary = idlProcessor.process(
        files, clientsDir, zipName, cmd, extraDirs
      );
      final String path = clientLibrary.getAbsolutePath();
      final File clientDir = new File(clientsDir(), zipName);
      final File renameDir = new File(clientsDir(), name);
      clientDir.renameTo(renameDir);
      LOG.debug("{} client library created at: {}", name, path);
    } catch (IOException e) {
      throw new ThriftStartupException(
        e, ThriftStartupMessage.STARTUP_009, alias.getName(), e.getMessage()
      );
    }
  }

  private File unzipLibraries() throws ThriftStartupException {
    final File libdir = new File(tempDir(), "lib");
    try {
      if (libdir.exists()) {
        FileUtil.deleteRecursively(libdir);
      }
      ThriftCompiler.unzipLibs(tempDir());
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_015, e.getMessage());
    }
    return libdir;
  }

}
