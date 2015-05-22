package org.thriftee.framework;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
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
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.compiler.schema.ThriftSchemaService;
import org.thriftee.framework.ThriftStartupException.ThriftStartupMessage;
import org.thriftee.framework.client.ClientTypeAlias;
import org.thriftee.provider.swift.SwiftSchemaBuilder;
import org.thriftee.util.New;
import org.thriftee.util.Strings;

import com.facebook.nifty.processor.NiftyProcessorAdapters;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.ThriftUnion;
import com.facebook.swift.codec.internal.ThriftCodecFactory;
import com.facebook.swift.codec.internal.coercion.DefaultJavaCoercions;
import com.facebook.swift.codec.internal.compiler.CompilerThriftCodecFactory;
import com.facebook.swift.codec.internal.reflection.ReflectionThriftCodecFactory;
import com.facebook.swift.service.ThriftEventHandler;
import com.facebook.swift.service.ThriftService;
import com.facebook.swift.service.ThriftServiceProcessor;

public class ThriftEE {

  private final Logger LOG = LoggerFactory.getLogger(getClass());

  public ThriftCodecManager codecManager() {
    return codecManager;
  }

  public ServiceLocator serviceLocator() {
    return serviceLocator;
  }

  public SortedMap<String, ClientTypeAlias> clientTypeAliases() {
    return clientTypeAliases;
  }

  public SortedMap<String, ProtocolTypeAlias> protocolTypeAliases() {
    return protocolTypeAliases;
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

  private final File tempDir;

  private final File idlDir;

  private final File thriftExecutable;

  private final File thriftLibDir;

  private final String thriftVersionString;

  private final ThriftCodecManager codecManager;

  private final Set<Class<?>> thriftStructs;

  private final Set<Class<?>> thriftServices;

  private final Set<Class<?>> thriftEnums;

  private final Set<Class<?>> thriftUnions;

  private final File[] idlFiles;

  private final File globalIdlFile;

  private final SortedMap<String, ClientTypeAlias> clientTypeAliases;
  
  private final SortedMap<String, ProtocolTypeAlias> protocolTypeAliases;

  private final SortedMap<String, TProcessor> processors;

  private final ServiceLocator serviceLocator;

  private final ThriftSchema schema;

  public ThriftEE(final ThriftEEConfig config) throws ThriftStartupException {

    this.tempDir = config.tempDir();
    this.idlDir = new File(tempDir, "idl");
    if (config.serviceLocator() != null) {
      this.serviceLocator = config.serviceLocator();
    } else {
      this.serviceLocator = new DefaultServiceLocator();
    }

    if (config.clientTypeAliases() == null) {
      this.clientTypeAliases = Collections.emptySortedMap();
    } else {
      this.clientTypeAliases = config.clientTypeAliases();
    }

    if (config.protocolTypeAliases() == null) {
      this.protocolTypeAliases = Collections.emptySortedMap();
    } else {
      this.protocolTypeAliases = config.protocolTypeAliases();
    }

    final AnnotationDB annotations = new AnnotationDB();
    annotations.setScanClassAnnotations(true);
    annotations.setScanFieldAnnotations(false);
    annotations.setScanMethodAnnotations(false);
    annotations.setScanParameterAnnotations(false);
    try {
      annotations.scanArchives(config.annotationClasspath().getUrls());
      thriftServices = searchFor(ThriftService.class, annotations);
      thriftStructs = searchFor(ThriftStruct.class, annotations);
      thriftUnions = searchFor(ThriftUnion.class, annotations);
      thriftEnums = searchFor(ThriftEnum.class, annotations);
    } catch (IOException e) {
      throw new ThriftStartupException(e, ThriftStartupMessage.STARTUP_002);
    }

    LOG.debug("Using bytecode compiler: {}", config.useBytecodeCompiler());
    final ThriftCodecFactory codecFactory;
    if (config.useBytecodeCompiler()) {
      codecFactory = new CompilerThriftCodecFactory(false);
    } else {
      codecFactory = new ReflectionThriftCodecFactory();
    }
    codecManager = new ThriftCodecManager(codecFactory);
    codecManager.getCatalog().addDefaultCoercions(DefaultJavaCoercions.class);

    LOG.debug("Initializing Thrift Services ----");
    LOG.debug("[Services detected]: {}", thriftServices);
    LOG.debug("[ Structs detected]: {}", thriftStructs);
    LOG.debug("[  Unions detected]: {}", thriftUnions);
    LOG.debug("[   Enums detected]: {}", thriftEnums);

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
        throw new ThriftStartupException(ThriftStartupMessage.STARTUP_007);
      }
      final File thriftExecutableFile = new File(thriftOnPath);
      this.thriftExecutable = thriftExecutableFile;
    }
    try {
      this.thriftVersionString = getVersionString();
    } catch (ThriftCommandException e) {
      throw new ThriftStartupException(
          e, ThriftStartupMessage.STARTUP_008, e.getMessage());
    }
    LOG.info("Using Thrift executable: {}", this.thriftExecutable);
    LOG.info("Thrift version string: {}", this.thriftVersionString);

    final Set<Class<?>> allClasses = new HashSet<Class<?>>();
    allClasses.addAll(thriftServices);
    allClasses.addAll(thriftStructs);
    allClasses.addAll(thriftUnions);
    allClasses.addAll(thriftEnums);

    //------------------------------------------------------------------//
    // We can easily fail-fast here by running an export of the         //
    // generated definitions.  If there are problems with the Thrift    //
    // schema, the export process will choke.                           //
    //------------------------------------------------------------------//
    LOG.debug("Exporting IDL files from Swift definitions");
    final File[] idlFiles;
    try {
      final ExportIDL exporter = new ExportIDL();
      idlFiles = exporter.export(idlDir, allClasses);
    } catch (final IOException e) {
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

    LOG.debug("Exporting configured clients");
    for (final ClientTypeAlias alias : clientTypeAliases().values()) {
      generateClientLibrary(alias);
    }

    LOG.debug("Setting up thrift processor map");
    try {
      final ThriftSchemaService impl = new ThriftSchemaService.Impl(schema());
      serviceLocator.register(ThriftSchemaService.class, impl);
    } catch (ServiceLocatorException e) {
      throw new ThriftStartupException(
          e, e.getThrifteeMessage(), e.getArguments());
    }
    this.processors = Collections.unmodifiableSortedMap(buildProcessorMap());

    LOG.info("Thrift initialization completed");
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

  public static String moduleNameFor(final String _packageName) {
    return _packageName.replace('.', '_');
  }

  public static String serviceNameFor(Class<?> c) {
    final ThriftService ann = c.getAnnotation(ThriftService.class);
    if (ann == null) {
      throw new IllegalArgumentException("not annotated with @ThriftService");
    }
    final String pkg = moduleNameFor(c.getPackage().getName());
    final String val = Strings.trimToNull(ann.value());
    final String svc = val == null ? c.getSimpleName() : val;
    return pkg + "." + svc;
  }

  private String getVersionString() {
    final ThriftCommand command = new ThriftCommand((Generate) null);
    command.setThriftCommand(this.thriftExecutable().getAbsolutePath());
    final ThriftCommandRunner run = ThriftCommandRunner.instanceFor(command);
    return run.executeVersion();
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
      final ProcessIDL idlProcessor = new ProcessIDL(alias);
      final String zipName = clientLibraryPrefix(name);
      final File clientLibrary = idlProcessor.process(
        files, tempDir(), zipName, cmd, extraDirs
      );
      final String path = clientLibrary.getAbsolutePath();
      LOG.debug("{} client library created at: {}", name, path);
    } catch (IOException e) {
      throw new ThriftStartupException(
        e, ThriftStartupMessage.STARTUP_009, alias.getName(), e.getMessage()
      );
    }
  }

  private SortedMap<String, TProcessor> buildProcessorMap() 
      throws ThriftStartupException {
    LOG.trace("Building processor map with svc locator: {}", serviceLocator);
    final SortedMap<String, TProcessor> processorMap = New.sortedMap();
    for (final Class<?> svcCls : thriftServices) {
      final String serviceName = serviceNameFor(svcCls);
      LOG.trace("Searching for impl of {} ({})", serviceName, svcCls);
      if (processorMap.containsKey(serviceName)) {
        throw new IllegalStateException(
          "found multiple instances of service: " + serviceName);
      }
      final Object impl;
      try {
        if (serviceLocator != null) {
          impl = serviceLocator.locate(svcCls);
        } else {
          impl = null;
        }
      } catch (final ServiceLocatorException e) {
        throw new ThriftStartupException(
            e, ThriftStartupMessage.STARTUP_010, svcCls, e.getMessage());
      }
      if (impl == null) {
        LOG.warn("No implementation found for service {}", serviceName);
        continue;
      }
      LOG.debug("Registering instance of {} as {}", impl, serviceName);
      final List<ThriftEventHandler> eventHandlers = Collections.emptyList();
      final ThriftServiceProcessor tsp = new ThriftServiceProcessor(
        codecManager, eventHandlers, impl
      );
      final TProcessor proc = NiftyProcessorAdapters.processorToTProcessor(tsp);
      processorMap.put(serviceName, proc);
    }
    return processorMap;
  }

}
