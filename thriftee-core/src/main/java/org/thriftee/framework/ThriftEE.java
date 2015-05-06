package org.thriftee.framework;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.compiler.ExportIDL;
import org.thriftee.compiler.ProcessIDL;
import org.thriftee.compiler.ThriftCommand;
import org.thriftee.compiler.ThriftCommand.Generate;
import org.thriftee.compiler.ThriftCommandException;
import org.thriftee.compiler.ThriftCommandRunner;
import org.thriftee.compiler.schema.SchemaBuilder;
import org.thriftee.compiler.schema.SchemaBuilderException;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.framework.ThriftStartupException.ThriftStartupMessage;
import org.thriftee.provider.swift.SwiftSchemaBuilder;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.ThriftUnion;
import com.facebook.swift.codec.internal.coercion.DefaultJavaCoercions;
import com.facebook.swift.codec.internal.compiler.CompilerThriftCodecFactory;
import com.facebook.swift.service.ThriftService;

public class ThriftEE {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public ThriftCodecManager codecManager() {
    return thriftCodecManager;
  }

  public SortedMap<String, ClientTypeAlias> clientTypeAliases() {
    return clientTypeAliases;
  }

  public Set<Class<?>> structs() {
    return thriftStructs;
  }

  public Set<Class<?>> services() {
    return thriftServices;
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
    final String prefix = clientLibraryPrefix(name);
    final File dir = new File(tempDir(), prefix);
    if (!dir.exists() || !dir.isDirectory()) {
      throw new IllegalStateException(
        "client dir does not exist: " + dir.getAbsolutePath());
    }
    return dir;
  }

  public File clientLibraryZip(final String name) {
    final String prefix = clientLibraryPrefix(name);
    final File zip = new File(tempDir(), prefix + ".zip");
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

  private final File tempDir;

  private final File idlDir;

  private final File thriftExecutable;

  private final File thriftLibDir;

  private final String thriftVersionString;

  private final ThriftCodecManager thriftCodecManager;

  private final Set<Class<?>> thriftStructs;

  private final Set<Class<?>> thriftServices;

  private final Set<Class<?>> thriftEnums;
  
  private final Set<Class<?>> thriftUnions;

  private final File[] idlFiles;
  
  private final File globalIdlFile;
  
  private final SortedMap<String, ClientTypeAlias> clientTypeAliases;
  
  private final ThriftSchema schema;

  public ThriftEE(ThriftEEConfig config) throws ThriftStartupException {

    this.tempDir = config.tempDir();
    this.idlDir = new File(tempDir, "idl");

    if (config.getClientTypeAliases() == null) {
      this.clientTypeAliases = Collections.emptySortedMap();
    } else {
      this.clientTypeAliases = config.getClientTypeAliases();
    }

    final AnnotationDB annotations = new AnnotationDB();
    annotations.setScanClassAnnotations(true);
    annotations.setScanFieldAnnotations(false);
    annotations.setScanMethodAnnotations(false);
    annotations.setScanParameterAnnotations(false);
    try {
      config.scannotationConfigurator().configure(annotations);
      thriftServices = searchFor(ThriftService.class, annotations);
      thriftStructs = searchFor(ThriftStruct.class, annotations);
      thriftUnions = searchFor(ThriftUnion.class, annotations);
      thriftEnums = searchFor(ThriftEnum.class, annotations);
    } catch (IOException e) {
      throw new ThriftStartupException(e, ThriftStartupMessage.STARTUP_002);
    }

    thriftCodecManager = new ThriftCodecManager(new CompilerThriftCodecFactory(false));
    thriftCodecManager.getCatalog().addDefaultCoercions(DefaultJavaCoercions.class);

    logger.debug("Initializing Thrift Services ----");
    logger.debug("[Services detected]: {}", thriftServices);
    logger.debug("[ Structs detected]: {}", thriftStructs);
    logger.debug("[  Unions detected]: {}", thriftUnions);
    logger.debug("[   Enums detected]: {}", thriftEnums);

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
    if (config.thriftLibDir() != null) {
      if (!config.thriftLibDir().exists()) {
        throw new ThriftStartupException(
          ThriftStartupMessage.STARTUP_005, config.thriftLibDir());
      } else if (!(validateThriftLibraryDir(config.thriftLibDir()))) {
        throw new ThriftStartupException(
          ThriftStartupMessage.STARTUP_006, config.thriftLibDir());
      } else {
        this.thriftLibDir = config.thriftLibDir();
      }
    } else {
      this.thriftLibDir = null;
    }
    logger.info("Thrift library dir: {}", thriftLibDir);

    //------------------------------------------------------------------//
    // Next we will validate the thrift executable and make note of the //
    // version that it returns when called.                             //
    //------------------------------------------------------------------//
    // TODO: If the native executable does not exist or cannot be called, 
    //       we should use NestedVM. Maybe use NestedVM no matter what.
    // TODO: Figure out a way to check the Thrift version against the version
    //       for the support libraries
    if (config.thriftExecutable() != null && config.thriftExecutable().exists()
        && config.thriftExecutable().canExecute()) {
      this.thriftExecutable = config.thriftExecutable();
      try {
        this.thriftVersionString = getVersionString();
      } catch (ThriftCommandException e) {
        throw new ThriftStartupException(
            e, ThriftStartupMessage.STARTUP_008, e.getMessage());
      }
    } else {
      this.thriftExecutable = null;
      this.thriftVersionString = null;
    }
    logger.info("Using Thrift executable: {}", thriftExecutable);
    logger.info("Thrift version string: {}", thriftVersionString);

    Set<Class<?>> allClasses = new HashSet<Class<?>>();
    allClasses.addAll(thriftServices);
    allClasses.addAll(thriftStructs);
    allClasses.addAll(thriftUnions);
    allClasses.addAll(thriftEnums);

    //------------------------------------------------------------------//
    // We can easily fail-fast here by running an export of the         //
    // generated definitions.  If there are problems with the Thrift    //
    // schema, the export process will choke.                           //
    //------------------------------------------------------------------//
    logger.debug("Exporting IDL files from Swift definitions");
    final File[] idlFiles;
    try {
      ExportIDL exporter = new ExportIDL();
      idlFiles = exporter.export(idlDir, allClasses);
    } catch (IOException e) {
      throw new ThriftStartupException(
          e, ThriftStartupMessage.STARTUP_001, e.getMessage());
    }
    this.idlFiles = idlFiles;

    //------------------------------------------------------------------//
    // At this point we will parse the generated IDL and store the meta //
    // model of the schema. Loosely typed clients or clients incapable  //
    // of introspection can use the meta model as a sort of reflection. //
    // ThriftEE specifically uses this to dynamically invoke services   //
    // from the ThriftEE dashboard.                                     //
    //------------------------------------------------------------------//
    
    File globalFile = null;
    for (int i = 0, c = idlFiles.length; globalFile == null && i < c; i++) {
      final File idlFile = idlFiles[i];
      if ("global.thrift".equals(idlFile.getName())) {
        globalFile = idlFile;
      }
    }

    if (globalFile == null) {
      throw new ThriftStartupException(ThriftStartupMessage.STARTUP_004); 
    } else {
      this.globalIdlFile = globalFile;
      try {
        SchemaBuilder schemaBuilder = new SwiftSchemaBuilder();
        this.schema = schemaBuilder.buildSchema(this);
      } catch (SchemaBuilderException e) {
        throw new ThriftStartupException(
            e, ThriftStartupMessage.STARTUP_003, e.getMessage());
      }
    }

    logger.debug("Exporting configured clients");
    for (final ClientTypeAlias alias : clientTypeAliases().values()) {
      generateClientLibrary(alias);
    }

    logger.info("Thrift initialization completed");
  }

  public static Set<Class<?>> searchFor(
      final Class<? extends Annotation> _ann, final AnnotationDB adb) {
    final Set<String> names = adb.getAnnotationIndex().get(_ann.getName());
    final Set<Class<?>> result = New.set();
    if (names != null) {
      for (String name : names) {
        try {
          final Class<?> clazz = Class.forName(name);
          result.add(clazz);
        } catch (ClassNotFoundException e) {
          LoggerFactory.getLogger(ThriftEE.class).warn(
            "warning: discovered @{} class via classpath scanning, " + 
            "but could not load: {}",
            _ann.getSimpleName(), 
            name
          );
        }
      }
    }
    return Collections.unmodifiableSet(result);
  }

  public static boolean validateThriftLibraryDir(File thriftLibDir) {
    if (thriftLibDir == null) {
      return false;
    } else {
      return new File(thriftLibDir, "php/lib/Thrift").exists();
    }
  }
  
  private String getVersionString() {
    ThriftCommand command = new ThriftCommand((Generate) null);
    command.setThriftCommand(this.thriftExecutable().getAbsolutePath());
    ThriftCommandRunner runner = ThriftCommandRunner.instanceFor(command);
    return runner.executeVersion();
  }

  private String clientLibraryPrefix(String name) {
    if (!clientTypeAliases().containsKey(name)) {
      throw new IllegalArgumentException("Invalid client type alias name");
    }
    return "client-" + name;
  }

  private void generateClientLibrary(ClientTypeAlias alias) 
      throws ThriftStartupException {
    final String name = alias.getName();
    logger.debug("Generating library for client type alias: {}", name);
    try {
      ThriftCommand cmd = new ThriftCommand(alias);
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
      final File clientLibrary = new ProcessIDL().process(
        files, tempDir(), clientLibraryPrefix(name), cmd, extraDirs
      );
      final String path = clientLibrary.getAbsolutePath();
      logger.debug("{} client library created at: {}", name, path);
    } catch (IOException e) {
      throw new ThriftStartupException(
        e, ThriftStartupMessage.STARTUP_009, alias.getName(), e.getMessage()
      );
    }
  }

}
