package org.thriftee.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.framework.ThriftEE;
import org.thriftee.framework.ThriftEEConfig;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.util.FileUtil;

public abstract class AbstractThriftEETest {

    private final File tempDirForClass;
    
    private final ThriftEE thrift;
    
    private static final Properties TEST_PROPERTIES;
    
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    
    static {
        final Logger logger = LoggerFactory.getLogger(AbstractThriftEETest.class);
        logger.trace("TRACE level enabled");
        logger.debug("DEBUG level enabled");
        logger.info( " INFO level enabled");
        logger.warn( " WARN level enabled");
        logger.error("ERROR level enabled");
        final ClassLoader loader = AbstractThriftEETest.class.getClassLoader();
        final URL propertiesResource = loader.getResource("thriftee.test.properties");
        if (propertiesResource != null) {
            InputStream inputStream = null;
            try {
                inputStream = propertiesResource.openStream();
                TEST_PROPERTIES = FileUtil.readProperties(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                FileUtil.forceClosed(inputStream);
            }
        } else {
            TEST_PROPERTIES = new Properties();
        }
    }
    
    public AbstractThriftEETest() throws ThriftStartupException {
        final String simpleName = getClass().getSimpleName();
        final File tempDir = new File("target/tests/" + simpleName);
        this.tempDirForClass = tempDir;
        final String thriftLibDir = TEST_PROPERTIES.getProperty(
            "thrift.lib.dir", 
            System.getProperty("thrift.lib.dir", "/usr/local/src/thrift/lib")
        );
        final String thriftExecutable = TEST_PROPERTIES.getProperty(
            "thrift.executable", 
            System.getProperty("thrift.executable", "/usr/local/bin/thrift")
        );
        thrift = new ThriftEE(
            (new ThriftEEConfig.Builder())
                .scannotationConfigurator(new TestScannotationConfigurator())
                .thriftLibDir(new File(thriftLibDir))
                .thriftExecutable(new File(thriftExecutable))
                .tempDir(tempDir)
                .build()
        );
    }
    
    protected File getTempDirForTest() {
        final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        File retval = new File(tempDirForClass, stackTraceElement.getMethodName());
        return retval;
    }
    
    protected ThriftEE thrift() {
        return thrift;
    }
    
}
