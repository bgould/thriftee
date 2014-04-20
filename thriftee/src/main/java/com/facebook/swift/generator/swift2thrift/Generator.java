package com.facebook.swift.generator.swift2thrift;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Generator {

    private Class<?>[] classes;

    private final Map<String, Set<Class<?>>> packageMap = new TreeMap<String, Set<Class<?>>>();

    private final Map<String, String> includeMap = new TreeMap<String, String>();

    private File tempDir;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public Generator() {
    }

    public void setClasses(Class<?>[] _classes) {
        this.classes = _classes;
    }

    public void setTempDir(File _tempDir) {
        if (!_tempDir.isDirectory() || !_tempDir.canWrite()) {
            throw new IllegalArgumentException("temp directory is not writeable");
        }
        tempDir = _tempDir;
    }

    public File generate() throws IOException {
        init();
        for (Map.Entry<String, Set<Class<?>>> pkg : packageMap.entrySet()) {
            File outputFile = new File(tempDir, makeThriftFilename(pkg.getKey()));
            Swift2ThriftGeneratorConfig config = createConfig(outputFile, pkg.getKey());
            Swift2ThriftGenerator generator = new Swift2ThriftGenerator(config);
            List<String> classNames = new ArrayList<String>(pkg.getValue().size());
            for (Class<?> klass : pkg.getValue()) {
                classNames.add(klass.getName());
            }
            generator.parse(classNames);
        }
        return tempDir;
    }

    private void init() throws IOException {
        packageMap.clear();
        includeMap.clear();
        if (tempDir == null) {
            tempDir = File.createTempFile("swift_generator_", "");
            tempDir.delete();
            tempDir.mkdir();
        }
        if (!tempDir.isDirectory() || !tempDir.canWrite()) {
            throw new IllegalStateException("temp directory is not writeable");
        }
        for (Class<?> klass : classes) {
            String packageName = klass.getPackage().getName();
            Set<Class<?>> set = packageMap.get(packageName);
            if (set == null) {
                set = new HashSet<Class<?>>();
                packageMap.put(packageName, set);
            }
            set.add(klass);
            includeMap.put(klass.getName(), makeThriftFilename(packageName));
        }
        LOG.info("[Generator] final include map: " + includeMap);
        LOG.info("[Generator] final package map: " + packageMap);
    }

    private Swift2ThriftGeneratorConfig createConfig(File _outputFile, String _packageName) {
        return Swift2ThriftGeneratorConfig.builder().outputFile(_outputFile).includeMap(includeMap)
                .defaultPackage(_packageName).
                // verbose(true).
                build();
    }

    private String makeThriftFilename(String _packageName) {
        return _packageName.replace('.', '_') + ".thrift";
    }

}
