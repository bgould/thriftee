package org.thriftee.framework;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.junit.Test;
import org.thriftee.examples.presidents.Name;
import org.thriftee.examples.presidents.President;
import org.thriftee.tests.AbstractThriftEETest;

import com.facebook.swift.codec.ThriftCodec;

public class ThriftEETest extends AbstractThriftEETest {

    public ThriftEETest() throws ThriftStartupException {
        super();
    }

    @Test
    public void testParsedIDL() throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.enable(SerializationFeature.INDENT_OUTPUT);
//        String json = mapper.writeValueAsString(thrift().schema());
//        LOG.debug(json);
        /*
        for (final String filename : thrift().parsedIDL().keySet()) {
            final Document parsedIDL = thrift().parsedIDL().get(filename);
            LOG.debug("Creating visitor for {}", filename);
            final DocumentVisitor visitor = new DocumentVisitor() {
                @Override
                public void visit(Visitable paramVisitable) throws IOException {
                    LOG.debug("visiting: {}", paramVisitable);
                }
                @Override
                public void finish() throws IOException {
                    LOG.debug("Finished visiting document.");
                }
                @Override
                public boolean accept(Visitable paramVisitable) {
                    return true;
                }
            };
            LOG.debug("Visiting {}", filename);
            parsedIDL.visit(visitor);
            
            LOG.debug("Serializing to JSON...");
            //String url = "http://freemusicarchive.org/api/get/albums.json?api_key=60BLHNQCAOUFPIBZ&limit=2";
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(parsedIDL);
            
            
            
//            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//            Albums albums = mapper.readValue(new URL(url), Albums.class);
//            Dataset[] datasets = albums.getDataset();
//            for (Dataset dataset : datasets) {
//                System.out.println(dataset.getAlbum_title());
//            }
        }
        */
    }
    
    @Test
    public void testWriteStruct() throws Exception {
        
        ThriftCodec<President> presidentCodec = thrift().codecManager().getCodec(President.class);
        Assert.assertNotNull(presidentCodec);
        
        President prez = new President();
        prez.setDied(new Date());
        prez.setBorn(new Date());
        prez.setName(new Name("Some", "F", "Guy"));
        prez.setEducation("Some College");
        prez.setPoliticalParty("New Year's");
        prez.setTerm("4 years");
        prez.setCareer("Party Animal");
        prez.setId(44);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TTransport transport = new TIOStreamTransport(baos);
        TProtocol protocol = new TSimpleJSONProtocol(transport);
        presidentCodec.write(prez, protocol);
        
        byte[] bytes = baos.toByteArray();
        Assert.assertTrue("byte array with result has length > 0", bytes.length > 0);
        
        LOG.debug("Serialized object: {}", new String(bytes));
        
    }
    
}
