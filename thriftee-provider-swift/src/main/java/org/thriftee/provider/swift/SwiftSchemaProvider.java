package org.thriftee.provider.swift;

import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_001;
import static org.thriftee.core.ThriftStartupException.ThriftStartupMessage.STARTUP_002;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.thrift.TProcessor;
import org.scannotation.AnnotationDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.core.Classpath;
import org.thriftee.core.SchemaProvider;
import org.thriftee.core.ServiceLocator;
import org.thriftee.core.ServiceLocatorException;
import org.thriftee.core.ThriftStartupException;
import org.thriftee.core.ThriftStartupException.ThriftStartupMessage;
import org.thriftee.core.util.FileUtil;
import org.thriftee.core.util.Strings;

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

public class SwiftSchemaProvider implements SchemaProvider {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final ThriftCodecManager codecManager;

  private final boolean useBytecodeCompiler;

  private final Classpath annotationClasspath;

  private final Set<Class<?>> thriftStructs;

  private final Set<Class<?>> thriftServices;

  private final Set<Class<?>> thriftEnums;

  private final Set<Class<?>> thriftUnions;

  public SwiftSchemaProvider(
      final boolean useBytecodeCompiler,
      final Classpath annotationClasspath) throws ThriftStartupException {

    this.useBytecodeCompiler = useBytecodeCompiler;
    this.annotationClasspath = annotationClasspath;

    final AnnotationDB annotations = new AnnotationDB();
    annotations.setScanClassAnnotations(true);
    annotations.setScanFieldAnnotations(false);
    annotations.setScanMethodAnnotations(false);
    annotations.setScanParameterAnnotations(false);
    try {
      annotations.scanArchives(annotationClasspath().getUrls());
      thriftServices = searchFor(ThriftService.class, annotations);
      thriftStructs = searchFor(ThriftStruct.class, annotations);
      thriftUnions = searchFor(ThriftUnion.class, annotations);
      thriftEnums = searchFor(ThriftEnum.class, annotations);
    } catch (IOException e) {
      throw new ThriftStartupException(e, STARTUP_002, e.getMessage());
    }

    LOG.debug("Using bytecode compiler: {}", useBytecodeCompiler());
    final ThriftCodecFactory codecFactory;
    if (useBytecodeCompiler()) {
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

  }

  @Override
  public File[] exportIdl(File idlDir) throws ThriftStartupException {

    final Set<Class<?>> allClasses = new HashSet<Class<?>>();
    allClasses.addAll(thriftServices);
    allClasses.addAll(thriftStructs);
    allClasses.addAll(thriftUnions);
    allClasses.addAll(thriftEnums);

    LOG.debug("Exporting IDL files from Swift definitions");
    final File[] idlFiles;
    try {
      final ExportIDL exporter = new ExportIDL();
      idlFiles = exporter.export(idlDir, allClasses);
      createIdlZip(idlDir, "swift");
      createIdlZip(idlDir, "thrift");
    } catch (final IOException e) {
      throw new ThriftStartupException(e, STARTUP_001, e.getMessage());
    }
    return idlFiles;

  }

  public ThriftCodecManager codecManager() {
    return codecManager;
  }

  public Classpath annotationClasspath() {
    return this.annotationClasspath;
  }

  public boolean useBytecodeCompiler() {
    return this.useBytecodeCompiler;
  }

  public static Set<Class<?>> searchFor(
      final Class<? extends Annotation> _ann, final AnnotationDB adb) {
    final Set<String> names = adb.getAnnotationIndex().get(_ann.getName());
    final Set<Class<?>> result = new HashSet<>();
    if (names != null) {
      for (String name : names) {
        try {
          final Class<?> clazz = Class.forName(name);
          result.add(clazz);
        } catch (ClassNotFoundException e) {
          LoggerFactory.getLogger(SwiftSchemaProvider.class).warn(
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

  @Override
  public SortedMap<String, TProcessor> buildProcessorMap(
        ServiceLocator serviceLocator
      ) throws ThriftStartupException {
    LOG.trace("Building processor map with svc locator: {}", serviceLocator);
    final SortedMap<String, TProcessor> processorMap = new TreeMap<>();
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

  private File createIdlZip(File idlDir, String type) throws IOException {
    final File dir = new File(idlDir, type);
    final File zip = new File(idlDir, "idl-" + type + ".zip");
    FileUtil.createZipFromDirectory(zip, "", dir);
    return zip;
  }

}
