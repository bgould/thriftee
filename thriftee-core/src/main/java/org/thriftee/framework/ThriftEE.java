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

import static org.thriftee.framework.ThriftStartupException.ThriftStartupMessage.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommandException;
import org.thriftee.compiler.ThriftCommandRunner;
import org.thriftee.compiler.schema.SchemaBuilder;
import org.thriftee.compiler.schema.SchemaBuilderException;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.compiler.schema.ThriftSchemaService;
import org.thriftee.framework.ThriftStartupException.ThriftStartupMessage;
import org.thriftee.framework.client.ClientTypeAlias;
import org.thriftee.thrift.compiler.ExecutionResult;
import org.thriftee.thrift.compiler.ThriftCompiler;
import org.thriftee.thrift.xml.Transforms;
import org.thriftee.thrift.xml.protocol.TXMLProtocol;
import org.thriftee.util.FileUtil;

public class ThriftEE implements SchemaBuilderConfig {

  public static final Charset XML_CHARSET = Charset.forName("UTF-8");

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  public ServiceLocator serviceLocator() {
    return serviceLocator;
  }

  public SortedMap<String, ClientTypeAlias> clientTypeAliases() {
    return clientTypeAliases;
  }

  public SortedMap<String, BaseProtocolTypeAlias> protocolTypeAliases() {
    return protocolTypeAliases;
  }

  public File thriftLibDir() {
    return this.thriftLibDir;
  }

  public File thriftExecutable() {
    return this.thriftExecutable;
  }

  public String thriftVersionString() {
    return this.thriftVersionString;
  }

  public File tempDir() {
    return this.tempDir;
  }

  public File clientsDir() {
    return this.clientsDir;
  }

  public File wsdlClientDir() {
    return this.wsdlClientDir;
  }

  public File idlDir() {
    return this.idlDir;
  }

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

  public File globalIdlFile() {
    return this.globalIdlFile;
  }

  public File globalXmlFile() {
    return this.globalXmlFile;
  }

  public Transforms xmlTransforms() {
    return this.transforms;
  }

  public TMultiplexedProcessor multiplexedProcessor() {
    LOG.debug("Building multiplexed processor");
    final TMultiplexedProcessor multiplex = new TMultiplexedProcessor();
    for (Entry<String, TProcessor> entry : this.processors.entrySet()) {
      if (entry.getKey() == null) {
        throw new IllegalStateException("One or more processors was null.");
      }
      LOG.debug("registering with multiplex processor: {}", entry.getKey());
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

  private final File tempDir;

  private final File clientsDir;

  private final File wsdlClientDir;

  private final File idlDir;

  private final File thriftExecutable;

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

    //------------------------------------------------------------------//
    // Here we are checking the configured Thrift library directory to  //
    // make sure that it actually contains code libraries for the       //
    // various target languages.  In actuality, the support libraries   //
    // are not strictly needed for using ThriftEE, however in almost    //
    // all scenarios that I can think of the best way to make sure that //
    // the same versions of the support library and the generated code  //
    // are used is to export both.  Especially for dynamic languages    //
    // like PHP, Python, Javascript, etc., it is very easy to           //
    // distribute the support library as part of the generated client.  //
    // For compiled languages like C++ and Java this is a bit less      //
    // useful but I do not think that the requirement that it exist is  //
    // too burdensome.  Can always change in the future if it proves to //
    // be problematic.                                                  //
    //------------------------------------------------------------------//
    // TODO: consider adding option to specify if missing dir is an error
    // TODO: consider making validation more robust 
    /*
    if (config.thriftLibDir() != null) {
      if (!config.thriftLibDir().exists()) {
        throw new ThriftStartupException(STARTUP_005, config.thriftLibDir());
      } else if (!(validateThriftLibraryDir(config.thriftLibDir()))) {
        throw new ThriftStartupException(STARTUP_006, config.thriftLibDir());
      } else {
        this.thriftLibDir = config.thriftLibDir();
      }
    } else {
      this.thriftLibDir = null;
    }
    */
    this.thriftLibDir = unzipLibraries();
    LOG.info("Thrift library dir: {}", thriftLibDir);

    //------------------------------------------------------------------//
    // Next we will validate the thrift executable and make note of the //
    // version that it returns when called.                             //
    //------------------------------------------------------------------//
    // TODO: If the native executable does not exist or cannot be called, 
    //       we should use NestedVM. Maybe use NestedVM no matter what.
    // TODO: Figure out a way to check the Thrift version against the version
    //       for the support libraries
    if (config.thriftExecutable() != null && 
        config.thriftExecutable().exists() && 
        config.thriftExecutable().canExecute()) {
      this.thriftExecutable = config.thriftExecutable();
    } else {
      final String thriftOnPath = ThriftCommand.searchPathForThrift();
      if (thriftOnPath == null) {
        throw new ThriftStartupException(STARTUP_007);
      }
      final File thriftExecutableFile = new File(thriftOnPath);
      this.thriftExecutable = thriftExecutableFile;
    }
    try {
      this.thriftVersionString = getVersionString();
    } catch (ThriftCommandException e) {
      throw new ThriftStartupException(e, STARTUP_008, e.getMessage());
    }
    LOG.info("Using Thrift executable: {}", this.thriftExecutable);
    LOG.info("Thrift version string: {}", this.thriftVersionString);

    idlFiles = config.schemaProvider().exportIdl(idlDir());
    File globalFile = null;
    for (int i = 0, c = idlFiles.length; globalFile == null && i < c; i++) {
      final File idlFile = idlFiles[i];
      if ("global.thrift".equals(idlFile.getName())) {
        globalFile = idlFile;
      }
    }
    if (globalFile == null) {
      throw new ThriftStartupException(STARTUP_004); 
    }
    this.globalIdlFile = globalFile;

    //------------------------------------------------------------------//
    // Generate the XML artifacts for thrift-to-SOAP conversion         //
    // TODO: this class is starting to smell bad; refactor to be leaner //
    // TODO: refactor XML to be "just another client"                   //
    //------------------------------------------------------------------//
    LOG.debug("Exporting XML definitions from IDL files");
    final File globalXml = new File(idlDir(), "global.xml");
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
    this.wsdlClientDir.mkdirs();
    try {
      transforms.preload(globalXmlFile);
      transforms.exportSchemas(globalXmlFile, this.wsdlClientDir);
      transforms.exportWsdls(globalXmlFile, this.wsdlClientDir);
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_014, e.getMessage());
    }

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
    try {
      final ThriftSchemaService impl = new ThriftSchemaService.Impl(schema());
      serviceLocator.register(ThriftSchemaService.class, impl);
    } catch (ServiceLocatorException e) {
      throw new ThriftStartupException(
          e, e.getThrifteeMessage(), e.getArguments());
    }
    this.processors = Collections.unmodifiableSortedMap(
      config.schemaProvider().buildProcessorMap(serviceLocator)
    );

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
    command.setThriftCommand(this.thriftExecutable().getAbsolutePath());
    final ThriftCommandRunner run = ThriftCommandRunner.instanceFor(command);
    return run;
  }

  private String getVersionString() {
    return newCommandRunner().executeVersion();
  }

  private String getHelpString() {
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

  private boolean generateGlobalXml(File out) throws ThriftStartupException {
    final File xmlDir = out.getParentFile();
    if (!xmlDir.exists()) {
      if (!xmlDir.mkdirs()) {
        throw new ThriftStartupException(STARTUP_011, String.format(
          "could not create directory for XML model output: %s", 
            xmlDir.getAbsolutePath()));
      }
    }
    final boolean nativeXmlSupported = isNativeXmlSupported();
    if (!nativeXmlSupported) {
      try {
        final Class<?> swiftParserXmlClass = Class.forName(
          "org.thriftee.provider.swift.SwiftParserXML"
        );
        final Method export = swiftParserXmlClass.getMethod(
          "export", new Class[] { File.class, Charset.class, Result.class}
        );
        LOG.debug("using Swift IDL parser to generate XML model output.");
        final Object swiftParserXml = swiftParserXmlClass.newInstance();
        final Charset utf8 = Charset.forName("UTF-8");
        if (utf8 == null) {
          throw new IllegalStateException("UTF-8 not found?");
        }
        try (final FileOutputStream fileout = new FileOutputStream(out)) {
          try (final Writer w = new OutputStreamWriter(fileout, utf8)) {
            final StreamResult streamResult = new StreamResult(w);
            export.invoke(swiftParserXml, new Object[] {
              globalIdlFile, utf8, streamResult
            });
          } catch (final InvocationTargetException e) {
            throw new ThriftStartupException(e, STARTUP_011, e.getMessage());
          }
        } catch (final IOException e) {
          throw new ThriftStartupException(e, STARTUP_011, e.getMessage());
        }
      } catch (ClassNotFoundException e) {
        throw new UnsupportedOperationException(
            "Thrift executable cannot create and XML model.");
      } catch ( IllegalAccessException|
                InstantiationException|
                NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      return true;
    } else {
      LOG.debug("using native Thrift compiler to generate XML model output.");
      final String path = globalIdlFile().getAbsolutePath();
      final ThriftCommand cmd = new ThriftCommand(Generate.XML, path);
      cmd.setThriftCommand(this.thriftExecutable().getAbsolutePath());
      cmd.setOutputLocation(xmlDir);
      cmd.addFlag(Generate.Flag.XML_MERGE);
      final ThriftCommandRunner runner = ThriftCommandRunner.instanceFor(cmd);
      final ExecutionResult result = runner.executeCommand();
      return result.successful();
    }
  }

  private boolean isNativeXmlSupported() {
    final String helpString = getHelpString();
    final StringReader str = new StringReader(helpString);
    try (BufferedReader reader = new BufferedReader(str)) {
      for (String line; (line = reader.readLine()) != null; ) {
        if (line.startsWith("  xml (XML)")) {
          return true;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  private void generateClientLibrary(ClientTypeAlias alias) 
      throws ThriftStartupException {
    final String name = alias.getName();
    LOG.debug("Generating library for client type alias: {}", name);
    try {
      final ThriftCommand cmd = new ThriftCommand(alias);
      cmd.setRecurse(true);
      if (thriftExecutable() != null) {
        cmd.setThriftCommand(thriftExecutable().getAbsolutePath());
      }
      final File[] extraDirs;
      if (thriftLibDir() != null && alias.getLibDir() != null) {
        final File libDir = new File(thriftLibDir(), alias.getLibDir());
        extraDirs = new File[] { libDir };
      } else {
        extraDirs = new File[0];
      }
      final File[] files = new File[] { globalIdlFile() };
      final ProcessIDL idlProcessor = new ProcessIDL(thriftLibDir(), alias);
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
    //final String rsrc = "org/apache/thrift/compiler/thrift-libs.zip";
    //final ClassLoader cl = ThriftCompiler.class.getClassLoader();
    final String rsrc = "thrift-libs.zip";
    final URL libzip = ThriftCompiler.class.getResource(rsrc); // cl.getResource(rsrc);
    if (libzip == null) {
      throw new IllegalStateException("could not find resource: " + rsrc);
    }
    final File libdir = new File(tempDir(), "lib");
    try {
      if (libdir.exists()) {
        FileUtil.deleteRecursively(libdir);
      }
      if (!libdir.mkdirs()) {
        throw new IllegalStateException(
          "could not create libdir: " + libdir.getAbsolutePath());
      }
      try (final InputStream raw = libzip.openStream()) {
        try (final ZipInputStream zip = new ZipInputStream(raw)) {
          final byte[] buffer = new byte[1024];
          for (ZipEntry entry; (entry = zip.getNextEntry()) != null; ) {
            final File file = new File(tempDir(), entry.getName());
            if (!entry.getName().startsWith("lib/")) {
              throw new IllegalStateException(
                "entry should start with lib/: " + entry.getName());
            }
            if (entry.isDirectory()) {
              file.mkdirs();
            } else {
              try (final FileOutputStream out = new FileOutputStream(file)) {
                for (int n = -1; (n = zip.read(buffer)) > -1; ) {
                  out.write(buffer, 0, n);
                }
              }
            }
          }
        }
      }
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_015, e.getMessage());
    }
    return libdir;
  }

}
