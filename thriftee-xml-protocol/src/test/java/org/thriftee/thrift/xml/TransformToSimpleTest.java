package org.thriftee.thrift.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.thrift.xml.Transformation.RootType;
import org.thriftee.thrift.xml.protocol.TXMLProtocol.Variant;
import org.thriftee.thrift.xml.protocol.TXMLProtocolTest;
import org.thriftee.thrift.xml.protocol.TestProtocol;
import org.thriftee.thrift.xml.protocol.UniverseImpl;

import everything.EndOfTheUniverseException;
import everything.Everything;
import everything.Universe;

@RunWith(Parameterized.class)
public class TransformToSimpleTest extends BaseThriftXMLTest {

  private final Variant variant;

  private File schemaFile;
  private File dataOutput;
  private File callOutput;
  private File transformOutput;
  private File replyOutput;
  private File transformOutput2;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      { Variant.VERBOSE },
      { Variant.CONCISE },
    });
  }

  public TransformToSimpleTest(Variant variant) {
    this.variant = variant;
  }

  @Before
  public void setup() {
    Assert.assertNotNull(testModelDir);
    Assert.assertNotNull(testMethodDir);
    dataOutput = new File(testMethodDir, "data.xml");
    schemaFile = new File(testModelDir, "everything.xml");
    callOutput = new File(testMethodDir, "call.xml");
    transformOutput = new File(testMethodDir, "call_output.xml");
    replyOutput = new File(testMethodDir, "reply.xml");
    transformOutput2 = new File(testMethodDir, "reply_output.xml");
  }

  @Test
  public void testTransformStruct() 
      throws TException, TransformerException, IOException {

    final TestProtocol protocol = createOutProtocol();
    final Everything everything = TXMLProtocolTest.everythingStruct();
    everything.write(protocol);

    final String xml = protocol.getStringOutput();
    System.out.println(xml);
    try (final FileWriter w = new FileWriter(dataOutput)) {
      Transforms.formatXml(xml, new StreamResult(w));
      w.flush();
    }

    final File transformOutput = new File(testMethodDir, "output.xml");
    try (final FileWriter w = new FileWriter(transformOutput)) {
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.STRUCT, 
        "Everything", 
        new StreamSource(dataOutput), 
        new StreamResult(w)
      );
      w.flush();
    }

    System.out.println("transform result: ");
    Transforms.formatXml(transformOutput, new StreamResult(System.out));
  }

  @Test
  public void testServiceCall() 
      throws TException, TransformerException, IOException {

    final Everything o = TXMLProtocolTest.everythingStruct();

    {
    final TestProtocol protocol = createOutProtocol();
    Universe.Client client = new Universe.Client.Factory().getClient(protocol);
    client.send_grok(o);
    protocol.writeOutputTo(callOutput);
    }

    System.out.println("\ncall:\n-----");
    Transforms.formatXml(callOutput, new StreamResult(System.out));

    try (final FileWriter w = new FileWriter(transformOutput)) {
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.MESSAGE, 
        "Universe", 
        new StreamSource(callOutput), 
        new StreamResult(w)
      );
    }

    System.out.println("\ntransformed call:\n----------------");
    Transforms.formatXml(transformOutput, new StreamResult(System.out));

    {
    final TestProtocol protocol = createOutProtocol(callOutput);
    final TProcessor processor = new Universe.Processor<>(new UniverseImpl());
    processor.process(protocol, protocol);
    protocol.writeOutputTo(replyOutput);
    }

    System.out.println("\nreply:\n-----");
    Transforms.formatXml(replyOutput, new StreamResult(System.out));

    try (final FileWriter w = new FileWriter(transformOutput2)) {
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.MESSAGE, 
        "Universe", 
        new StreamSource(replyOutput), 
        new StreamResult(w)
      );
    }

    System.out.println("\ntransformed reply:\n----------------");
    Transforms.formatXml(transformOutput2, new StreamResult(System.out));

  }

  @Test
  public void testApplicationException () throws 
      TException, IOException, TransformerException {

    final TestProtocol protocol = createOutProtocol();
    final TApplicationException x = new TApplicationException(
      TApplicationException.INTERNAL_ERROR, 
      "An internal error occured."
    );
    protocol.writeMessageBegin(new TMessage("woah", TMessageType.EXCEPTION, 1));
    x.write(protocol);
    protocol.writeMessageEnd();
    protocol.getTransport().flush();
    protocol.writeOutputTo(replyOutput);

    System.out.println("\nexception:\n-----");
    Transforms.formatXml(replyOutput, new StreamResult(System.out));

    try (final FileWriter w = new FileWriter(transformOutput)) {
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.MESSAGE, 
        "Universe", 
        new StreamSource(replyOutput), 
        new StreamResult(w)
      );
    }

    System.out.println("\ntransformed exception:\n----------------");
    Transforms.formatXml(transformOutput, new StreamResult(System.out));

  }

  @Test
  public void testException() 
      throws TException, TransformerException, IOException {

    final Everything o = TXMLProtocolTest.everythingStruct();

    {
    final TestProtocol protocol = createOutProtocol();
    Universe.Client client = new Universe.Client.Factory().getClient(protocol);
    client.send_grok(o);
    protocol.writeOutputTo(callOutput);
    }

    System.out.println("\ncall:\n-----");
    Transforms.formatXml(callOutput, new StreamResult(System.out));

    try (final FileWriter w = new FileWriter(transformOutput)) {
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.MESSAGE, 
        "Universe", 
        new StreamSource(callOutput), 
        new StreamResult(w)
      );
    }

    System.out.println("\ntransformed call:\n----------------");
    Transforms.formatXml(transformOutput, new StreamResult(System.out));

    {
    final TestProtocol protocol = createOutProtocol(callOutput);
    final TProcessor processor = new Universe.Processor<>(new UniverseImpl() {
      @Override
      public int grok(Everything arg0) throws TException {
        throw new EndOfTheUniverseException("its over!!");
      }
    });
    processor.process(protocol, protocol);
    protocol.writeOutputTo(replyOutput);
    }

    System.out.println("\nreply:\n-----");
    Transforms.formatXml(replyOutput, new StreamResult(System.out));

    try (final FileWriter w = new FileWriter(transformOutput2)) {
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.MESSAGE, 
        "Universe", 
        new StreamSource(replyOutput), 
        new StreamResult(w)
      );
    }

    System.out.println("\ntransformed reply:\n----------------");
    Transforms.formatXml(transformOutput2, new StreamResult(System.out));

  }
  
  /* just shoving this here for convenience ... should be somewhere else */
  public void test10000Transforms() throws Exception {
    final TestProtocol protocol = createOutProtocol();
    final Everything everything = TXMLProtocolTest.everythingStruct();
    everything.write(protocol);

    final String xml = protocol.getStringOutput();

    // get one transformer to be sure it is precompiled
    Transforms.newStreamingToSimpleTransformer();

    final int count = 10000;
    long totalNanos = 0;
    for (int i =0 ; i < count; i++) {
      final long startNano = System.nanoTime();
      Transforms.transformStreamingToSimple(
        schemaFile, 
        "everything", 
        RootType.STRUCT, 
        "Everything", 
        new StreamSource(new StringReader(xml)), 
        new StreamResult(new StringWriter())
      );
      totalNanos += System.nanoTime() - startNano;
    }

    System.out.printf("Avg ms per %s transform: %s%n",
      variant.name(), ((double) totalNanos / (double) count) * (1e-6)
    );
  }

  @Override
  protected Variant variant() {
    return variant;
  }

}