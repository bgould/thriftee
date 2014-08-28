package org.thriftee.compiler.schema;

import org.junit.Test;
import org.thriftee.framework.ThriftStartupException;
import org.thriftee.tests.AbstractThriftEETest;

import static org.junit.Assert.*;

public class SchemaBuilderTest extends AbstractThriftEETest {

    public SchemaBuilderTest() throws ThriftStartupException {
        super();
    }

    @Test
    public void testExampleSchema() {
        
        ThriftSchema schema = thrift().schema();
        assertNotNull("schema must not be null", schema);
        assertNotNull("modules collection must not be null", schema.getModules());
        assertTrue("number of modules must be greater than 1", schema.getModules().size() > 1);
        LOG.debug("modules in schema: {}", schema.getModules());
        
        ModuleSchema presidents = schema.getModules().get("org_thriftee_examples_presidents");
        assertNotNull("presidents module must not be null", presidents);
        assertNotNull("presidents module must have enums", presidents.getEnums().size() > 0);
        LOG.debug("enums in module: {}", presidents.getEnums());
        
        EnumSchema sortOrderSchema = presidents.getEnums().get("SortOrder");
        assertNotNull("presidents module must have sort order", sortOrderSchema);
        LOG.debug("sort order enum: {}", sortOrderSchema);
        
        StructSchema presidentStruct = presidents.getStructs().get("President");
        assertNotNull("President struct must not be null", presidentStruct);
        LOG.debug("president struct: {}", presidentStruct);
        LOG.debug("president struct protocol type: {}", presidentStruct.getProtocolType());
        LOG.debug("fields on president struct: {}", presidentStruct.getFields());
        
        
        
    }
    
}
