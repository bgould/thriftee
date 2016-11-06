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

import static org.thriftee.examples.Examples.everythingStruct;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Assert;
import org.thriftee.thrift.protocol.TJsonApiProtocol;
import org.thriftee.thrift.protocol.TXMLProtocol;
import org.thriftee.thrift.protocol.xml.BaseThriftProtocolTest;
import org.thriftee.thrift.schema.IdlSchemaBuilder;
import org.thriftee.thrift.schema.SchemaBuilderException;
import org.thriftee.thrift.schema.StructSchema;

import everything.Everything;

public class SpeedTest extends BaseThriftProtocolTest {

  public static StructSchema structSchema() throws IOException {
    final File modelFile = new File("target/tests/models/everything.xml");
    final IdlSchemaBuilder bldr = new IdlSchemaBuilder();
    try {
      return bldr.buildFromXml(new StreamSource(modelFile)).
        findModule("everything").getStructs().get("Everything");
    } catch (SchemaBuilderException e) {
      throw new IOException(e);
    }
  }

  public static Collection<TProtocolFactory> data() throws IOException {
    return Arrays.asList(new TProtocolFactory[] {
      new TCompactProtocol.Factory(),
      new TBinaryProtocol.Factory(),
      new TJSONProtocol.Factory(),
      new TXMLProtocol.Factory(),
      new TJsonApiProtocol.Factory(structSchema()),
    });
  }

  public static void main(String[] args) throws Exception {
    System.out.printf("exporting models, etc...");
    beforeClass();
    System.out.printf("done%n%n");
    for (TProtocolFactory fctry : data()) {
      final SpeedTest test = new SpeedTest(fctry);
      test.testSpeed();
    }
  }

  private final TProtocolFactory factory;

  public SpeedTest(TProtocolFactory factory) {
    this.factory = factory;
  }

  public void testSpeed() throws TException, IOException {

    final int warmup = 2000;
    final int count = 100000;
    final String name = factory.getClass().getEnclosingClass().getSimpleName();

    long readNanos = 0;
    long writeNanos = 0;
    //long totalNanos = 0;
    final Everything struct = everythingStruct();

    System.out.printf("Warming up %s%n", name);
    for (int i =0 ; i < warmup; i++) {
      final byte[] arr;
      {
        final TByteArrayOutputStream baos = new TByteArrayOutputStream(4096);
        final TTransport out = new TIOStreamTransport(baos);
        final TProtocol outProtocol = factory.getProtocol(out);
        arr = baos.get();
        struct.write(outProtocol);
      }
      if (i == 0 && arr.length == 0) {
        Assert.fail("array was zero length");
      }
      //System.out.println(new String(arr)); if (true) return;
      {
        final Everything struct2 = new Everything();
        final ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        final TTransport in = new TIOStreamTransport(bais);
        final TProtocol inProtocol = factory.getProtocol(in);
        struct2.read(inProtocol);
      }
    }

    System.gc();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {}

    System.out.printf("Starting test for %s (%s rounds)...%n", name, count);
    for (int i =0 ; i < count; i++) {
      final byte[] arr;
      {
        final TByteArrayOutputStream baos = new TByteArrayOutputStream(4096);
        final TTransport out = new TIOStreamTransport(baos);
        final TProtocol outProtocol = factory.getProtocol(out);
        arr = baos.get();

        final long writeStart = System.nanoTime();
        struct.write(outProtocol);
        writeNanos += System.nanoTime() - writeStart;
      }
      if (i == 0 && arr.length == 0) {
        Assert.fail("array was zero length");
      }
      //System.out.println(new String(arr)); if (true) return;
      {
        final Everything struct2 = new Everything();
        final ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        final TTransport in = new TIOStreamTransport(bais);
        final TProtocol inProtocol = factory.getProtocol(in);

        final long readStart = System.nanoTime();
        struct2.read(inProtocol);
        readNanos += System.nanoTime() - readStart;
      }
    }

    System.out.printf("  Avg ms per read:  %s%n",
      ((double) readNanos / (double) count) * (1e-6)
    );
    System.out.printf("  Avg ms per write: %s%n",
      ((double) writeNanos / (double) count) * (1e-6)
    );
    System.out.printf("  Avg round trip:   %s%n",
      ((double) (writeNanos + readNanos) / (double) count) * (1e-6)
    );
    System.out.printf("  Total time:       %s%n",
      (writeNanos + readNanos) * (1e-6)
    );
    System.out.println();
  }


}
