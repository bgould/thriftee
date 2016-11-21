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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.thriftee.examples.Examples;
import org.thriftee.thrift.protocol.xml.BaseThriftProtocolTest;
import org.thriftee.thrift.protocol.xml.Transformation.RootType;
import org.thriftee.thrift.protocol.xml.Transforms;

import everything.Universe.grok_args;

public class SpeedTest2 extends BaseThriftProtocolTest {

  public static void main(String[] args) throws Exception {
    System.out.printf("exporting models, etc...");
    beforeClass();
    System.out.printf("done%n%n");
    final SpeedTest2 test = new SpeedTest2();
    test.testSpeed();
  }

  public void testSpeed() throws TException, IOException {

    final int round = 1000;
    final int count = 10000;

    final File modelFile = modelFor("everything");
    final Transforms transforms = new Transforms();
    transforms.preload(modelFile);
    transforms.newSimpleToStreaming();
    transforms.newStreamingToSimple();

    final grok_args struct = Examples.grokArgs();
    BaseThriftProtocolTest.filter(struct.everything);

    long readNanos = 0;
    long writeNanos = 0;

    long readNanosTotal = 0;
    long writeNanosTotal = 0;

    for (int i = 1 ; i <= count; i++) {

      final byte[] xmlBytes;
      {
        final long writeStart = System.nanoTime();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final TTransport transport = new TIOStreamTransport(baos);
        final TXMLProtocol protocol = new TXMLProtocol(transport);
        protocol.writeMessageBegin(new TMessage("grok", (byte)1, 1));
        struct.write(protocol);
        protocol.writeMessageEnd();
        final byte[] thriftBytes = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(thriftBytes);
        final ByteArrayOutputStream xml = new ByteArrayOutputStream();
        transforms.newStreamingToSimple(
          modelFile, "everything", RootType.MESSAGE, "Universe"
        ).transform(new StreamSource(bais), new StreamResult(xml));;
        xmlBytes = xml.toByteArray();
        writeNanos += System.nanoTime() - writeStart;
      }

      final grok_args struct2 = new grok_args();
      {
        final long readStart = System.nanoTime();
        final ByteArrayInputStream xml = new ByteArrayInputStream(xmlBytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transforms.newSimpleToStreaming(
          modelFile, "everything", false
        ).transform(new StreamSource(xml), new StreamResult(baos));
        final byte[] thriftBytes = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(thriftBytes);
        final TTransport transport = new TIOStreamTransport(bais);
        final TXMLProtocol protocol = new TXMLProtocol(transport);
        protocol.readMessageBegin();
        struct2.read(protocol);
        protocol.readMessageEnd();
        readNanos += System.nanoTime() - readStart;
      }

      if (i == 0) {
        Assert.assertEquals(struct, struct2);
      }


      if (i % round == 0) {
        readNanosTotal += readNanos;
        writeNanosTotal += writeNanos;
        System.out.println("--------- Completed "+i+" rounds ---------------");
        System.out.printf("  Avg ms per read:          %s%n",
          ((double) readNanos / (double) round) * (1e-6)
        );
        System.out.printf("  Avg ms per write:         %s%n",
          ((double) writeNanos / (double) round) * (1e-6)
        );
        System.out.printf("  Avg round trip:           %s%n",
          ((double) (writeNanos + readNanos) / (double) round) * (1e-6)
        );
        System.out.printf("  Cycle time:               %s%n",
          (writeNanos + readNanos) * (1e-6)
        );
        System.out.println();
        System.out.printf("  Avg ms per read (total):  %s%n",
          ((double) readNanosTotal / (double) i) * (1e-6)
        );
        System.out.printf("  Avg ms per write (total): %s%n",
          ((double) writeNanosTotal / (double) i) * (1e-6)
        );
        System.out.printf("  Avg round trip (total):   %s%n",
          ((double) (writeNanosTotal + readNanosTotal) / (double) i) * (1e-6)
        );
        System.out.printf("  Total time:               %s%n",
          (writeNanosTotal + readNanosTotal) * (1e-6)
        );
        System.out.println("-----------------------------------------------\n");
        readNanos = 0;
        writeNanos = 0;
      }
    }

  }


}
