package org.thriftee.tests;

import java.io.IOException;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.thriftee.compiler.schema.ThriftSchema;
import org.thriftee.examples.presidents.President;
import org.thriftee.framework.ScannotationConfigurator;

public class TestScannotationConfigurator implements ScannotationConfigurator {

    @Override
    public void configure(AnnotationDB db) throws IOException {
        db.scanArchives(ClasspathUrlFinder.findClassBase(President.class));
        db.scanArchives(ClasspathUrlFinder.findClassBase(ThriftSchema.class));
    }

}
