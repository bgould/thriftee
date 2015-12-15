package org.thriftee.thrift.xml;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;

import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.TBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.thrift.xml.protocol.TestProtocol;
import org.thriftee.thrift.xml.protocol.TXMLProtocol.Variant;

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

    // verbose.xml is the message before having any transformations applied
    final File streamed = testobj.verboseXml();
    final String frmtd1 = Transforms.formatXml(new StreamSource(streamed));

    // streaming.xml is the streaming protocol after round-tripping through xslt
    final File trnsfrmd = testobj.streamingXml();
    final String frmtd2 = Transforms.formatXml(new StreamSource(trnsfrmd));

    assertEquals("formatted XML should equal after round trip", frmtd1, frmtd2);

    if (testobj instanceof TestCall) {
      //final TestCall test = (TestCall) testobj;
      // TODO: test 
      
    } else {
      final TestProtocol iprot1 = new TestProtocol(frmtd1, Variant.VERBOSE);
      final TBase<?, ?> obj1 = (TBase<?,?>)testobj.obj.getClass().newInstance();
      obj1.read(iprot1);

      final TestProtocol iprot2 = new TestProtocol(frmtd2, Variant.VERBOSE);
      final TBase<?, ?> obj2 = (TBase<?,?>)testobj.obj.getClass().newInstance();
      obj2.read(iprot2);

      assertEquals(obj1, obj2);
      assertEquals(obj1, testobj.obj);
      assertEquals(obj2, testobj.obj);
    }
  }

}
