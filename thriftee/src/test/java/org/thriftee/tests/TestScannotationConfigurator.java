package org.thriftee.tests;

import java.io.IOException;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;
import org.thriftee.examples.presidents.President;
import org.thriftee.framework.ScannotationConfigurator;

public class TestScannotationConfigurator implements ScannotationConfigurator {

    @Override
    public void configure(AnnotationDB db) throws IOException {
        db.scanArchives(ClasspathUrlFinder.findClassBase(President.class));
    }

}
