/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.thrift.protocol;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.thriftee.examples.Examples;
import org.thriftee.thrift.protocol.xml.BaseThriftXMLTest;
import org.thriftee.thrift.protocol.xml.Transforms;

import everything.Everything;

public class TSoapXmlProtocolTest extends BaseThriftXMLTest {

  public final TSoapXmlProtocol.Factory factory; {
    final Transforms transforms = new Transforms();
    factory = new TSoapXmlProtocol.Factory();
    factory.setTransforms(transforms);
    factory.setModelFile(modelFor("everything"));
    factory.setModuleName("everything");
    factory.setStructName("Everything");
    factory.setServiceName("Universe");
    try {
      transforms.preload(factory.getModelFile());
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  @Test
  public void testStructRoundtrip()
      throws TException, TransformerException, IOException {

    final TMemoryBuffer buffer = new TMemoryBuffer(4096);
    final TSoapXmlProtocol writeProtocol = factory.getProtocol(buffer);
    final Everything test = Examples.everythingStruct();
    test.write(writeProtocol);

    final String xml = new String(buffer.getArray(), 0, buffer.length());
    final TTransport transport = new TMemoryInputTransport(xml.getBytes());
    final TSoapXmlProtocol readProtocol = factory.getProtocol(transport);
    final Everything test2 = new Everything();
    test2.read(readProtocol);

    assertEquals("structs should be equal after roundtrip", test, test2);

  }
/*
  @Test
  public void testServiceCall()
      throws TException, TransformerException, IOException {

    final Everything o = everythingStruct();

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

    final Everything o = everythingStruct();

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
*/
}
