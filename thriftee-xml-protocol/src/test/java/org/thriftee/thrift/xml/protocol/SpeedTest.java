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
package org.thriftee.thrift.xml.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

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

import everything.Everything;

//@RunWith(Parameterized.class)
public class SpeedTest {

  //@Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      new Object[] { new TCompactProtocol.Factory() },
      new Object[] { new TBinaryProtocol.Factory() },
      new Object[] { new TJSONProtocol.Factory() },
      new Object[] { new TXMLProtocol.Factory() },
    });
  }
  
  private final TProtocolFactory factory;

  public SpeedTest(TProtocolFactory factory) {
    this.factory = factory;
  }

  //@Test
  public void testSpeed() throws TException, IOException {

    final int count = 100000;
    System.out.printf(
      "Starting test for %s (%s rounds)...%n", 
      factory.getClass().getEnclosingClass().getSimpleName(), 
      count
    );

    long readNanos = 0;
    long writeNanos = 0;
    //long totalNanos = 0;
    final Everything struct = TXMLProtocolTest.everythingStruct();

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
      ((double) (writeNanos + readNanos)) * (1e-6)
    );
    System.out.println();
  }

  
}
