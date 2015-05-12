package org.thriftee.restlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.thriftee.compiler.schema.MethodArgumentSchema;
import org.thriftee.compiler.schema.MethodSchema;
import org.thriftee.compiler.schema.ModuleSchema;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.tests.AbstractThriftEETest;
import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftCodec;
import com.facebook.swift.codec.ThriftCodecManager;

public class ThriftProcessorRepresentationTest extends AbstractThriftEETest {

  private static final String MODULE = "org_thriftee_examples_usergroup_service";

  @Test
  public void testBinaryProcessor() throws TException, IOException {
    TProtocolFactory factory = new TBinaryProtocol.Factory();
    testProcessor(factory);
  }

  @Test
  public void testJsonProcessor() throws TException, IOException {
    TProtocolFactory factory = new TJSONProtocol.Factory();
    testProcessor(factory);
  }

  @Test
  public void testCompactProcessor() throws TException, IOException {
    TProtocolFactory factory = new TCompactProtocol.Factory();
    testProcessor(factory);
  }

  @Test
  public void testTupleProcessor() throws TException, IOException {
    TProtocolFactory factory = new TTupleProtocol.Factory();
    testProcessor(factory);
  }

  void testProcessor(TProtocolFactory factory) throws TException, IOException {

    ThriftCodecManager mgr = thrift().codecManager();
    ModuleSchema module = thrift().schema().getModules().get(MODULE);
    ServiceSchema service = module.getServices().get("UserService");
    MethodSchema method = service.getMethods().get("find");

    Map<String, Object> args = New.map();
    args.put("uid", "aaardvark");
    byte[] serviceCall = createServiceCall(mgr, factory, method, args);

    Representation in = new ByteArrayRepresentation(serviceCall);
    ThriftProcessorRepresentation rep = new ThriftProcessorRepresentation(
      in, factory, factory, thrift().processorFor(service)
    );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    rep.write(out);

  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static byte[] createServiceCall(
      ThriftCodecManager mgr,
      TProtocolFactory factory,
      MethodSchema method, 
      Map<String, Object> args
    ) throws TException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TTransport transport = new TIOStreamTransport(baos);
    TProtocol protocol = factory.getProtocol(transport);
    TMessage msg = new TMessage(method.getName(), TMessageType.CALL, 0);
    protocol.writeMessageBegin(msg);
    protocol.writeStructBegin(new TStruct("__fixme__"));
    if (args != null) {
      for (Entry<String, Object> entry : args.entrySet()) {
        final String key = entry.getKey();
        final Object arg = entry.getValue();
        final MethodArgumentSchema argSchema = method.getArguments().get(key);
        if (arg == null) {
          throw new IllegalArgumentException("argument cannot be null.");
        }
        if (argSchema == null) {
          throw new IllegalArgumentException(
            "no schema for arg: " + key + " : " + 
            method.getArguments().keySet()
          );
        }
        final ThriftCodec codec = mgr.getCodec(arg.getClass());
        if (codec == null) {
          throw new IllegalArgumentException("no codec for " + arg.getClass());
        }
        byte codecType = codec.getType().getProtocolType().getType();
        byte schemaType = argSchema.getType().getProtocolType().getType();
        if (codecType != schemaType) {
          throw new IllegalArgumentException(
            "codec and schema types do not match for arg: " + key);
        }
        final TField field = new TField(
          argSchema.getName(), 
          argSchema.getType().getProtocolType().getType(), 
          argSchema.getIdentifier().shortValue()
        );
        protocol.writeFieldBegin(field);
        try {
          codec.write(arg, protocol);
        } catch (Exception e) {
          throw new TException(e);
        }
        protocol.writeFieldEnd();
      }
    }
    protocol.writeFieldStop();
    protocol.writeStructEnd();
    protocol.writeMessageEnd();

    final byte[] bytes = baos.toByteArray();
    return bytes;

  }
}
