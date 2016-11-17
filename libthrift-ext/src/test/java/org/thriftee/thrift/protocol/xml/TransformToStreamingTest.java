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
package org.thriftee.thrift.protocol.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TSerializable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.thrift.protocol.TestProtocol;

@RunWith(Parameterized.class)
public class TransformToStreamingTest extends BaseThriftXMLTest {

  @Parameters
  public static Collection<Object[]> data() {
    return testParameters();
  }

  private final TestObject testobj;

  public TransformToStreamingTest(TestObject testobj) {
    super();
    this.testobj = testobj;
  }

  @Test
  public void structTest() throws Exception {

    // concise.xml is the message before having any transformations applied
    final File streamed = testobj.conciseXml();
    final String frmtd1 = Transforms.formatXml(new StreamSource(streamed));

    // streaming.xml is the streaming protocol after round-tripping through xslt
    final File trnsfrmd = testobj.streamingXml();
    final String frmtd2 = Transforms.formatXml(new StreamSource(trnsfrmd));

    System.out.println(frmtd1);
    System.out.println(frmtd2);

    assertEquals("formatted XML should equal after round trip", frmtd1, frmtd2);

    if (testobj instanceof TestCall) {
      //final TestCall test = (TestCall) testobj;
      // TODO: test

    } else {
      final TestProtocol iprot1 = new TestProtocol(frmtd1);
      final TSerializable obj1 = testobj.obj.getClass().newInstance();
      obj1.read(iprot1);

      final TestProtocol iprot2 = new TestProtocol(frmtd2);
      final TSerializable obj2 = testobj.obj.getClass().newInstance();
      obj2.read(iprot2);

      assertEquals(obj1, obj2);
      assertEquals(obj1, testobj.obj);
      assertEquals(obj2, testobj.obj);
    }
  }

}
