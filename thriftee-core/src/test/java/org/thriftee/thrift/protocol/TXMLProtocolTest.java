package org.thriftee.thrift.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.thriftee.compiler.schema.ServiceSchema;
import org.thriftee.examples.classicmodels.Customer;
import org.thriftee.examples.classicmodels.Office;
import org.thriftee.examples.classicmodels.Order;
import org.thriftee.examples.classicmodels.OrderDetail;
import org.thriftee.examples.classicmodels.OrderDetailPK;
import org.thriftee.tests.AbstractThriftEETest;
import org.thriftee.thrift.protocol.Everything.Sparkle;
import org.thriftee.thrift.protocol.Everything.Spinkle;
import org.thriftee.thrift.protocol.TXMLProtocol.Variant;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class TXMLProtocolTest extends AbstractThriftEETest {

  private ByteArrayInputStream inStream;

  private ByteArrayOutputStream outStream;

  private final TXMLProtocol.Factory factory;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      { new TXMLProtocol.Factory(Variant.VERBOSE, true) },
      { new TXMLProtocol.Factory(Variant.CONCISE, true) },
    });
  }

  public TXMLProtocolTest(TXMLProtocol.Factory factory) {
    this.factory = factory;
  }

  public void testWrite1() throws Exception {
    final TXMLProtocol protocol = createOutProtocol(factory);
    {
      thrift().codecManager().write(Office.class, testStruct1(), protocol);
      final String serialized = formatXml(new String(outStream.toByteArray()));
      System.out.println("Serialized:\n-----------------------\n" + serialized);
      System.out.println(serialized);
      validate(protocol, serialized);
    }
    outStream.reset();
    {
      thrift().codecManager().write(Office.class, testStruct1(), protocol);
      final String serialized = formatXml(new String(outStream.toByteArray()));
      System.out.println("Serialized:\n-----------------------\n" + serialized);
      System.out.println(serialized);
      validate(protocol, serialized);
    }
  }

  public void validate(TXMLProtocol protocol, String msg) throws SAXException, IOException {
    System.out.print("Validating against " + protocol.schemaUrl() + " ...");
    final String validationError = protocol.validate(msg);
    if (validationError != null) {
      System.out.println(" invalid!\n" + validationError + "\n");
      fail(validationError);
    }
    System.out.println(" valid.\n\n");
  }

  public void testWrite2() throws Exception {
    TProtocol protocol = createOutProtocol(factory);
    thrift().codecManager().write(Order.class, testStruct2(), protocol);
    final String serialized = formatXml(new String(outStream.toByteArray()));
    System.out.println(serialized);
  }

  @Test
  public void testEverything() throws Exception {
    final Everything struct = everythingStruct();
    testRoundtrip(Everything.class, struct, factory);
  }

  public void testRoundTrip1() throws Exception {
    testRoundtrip(Office.class, testStruct1(), factory);
  }

  public void testRoundTrip2() throws Exception {
    testRoundtrip(Order.class, testStruct2(), factory);
  }

  @Test
  public void testService() throws Exception {

    final Class<Everything> cl = Everything.class;
    final Everything o = everythingStruct();

    final TXMLProtocol protocol = createOutProtocol(factory);
    final TMessage tmsg = new TMessage("grok", TMessageType.CALL, 0);
    final TStruct args = new TStruct("grok_args");
    final TField arg0 = new TField("arg0", TType.STRUCT, (short)1);
    
    protocol.writeMessageBegin(tmsg);
    protocol.writeStructBegin(args);
    protocol.writeFieldBegin(arg0);
    thrift().codecManager().write(cl, o, protocol);
    protocol.writeFieldEnd();
    protocol.writeFieldStop();
    protocol.writeStructEnd();
    protocol.writeMessageEnd();

    final String serialized = formatXml(new String(outStream.toByteArray()));
    System.out.println("Request:\n-----------------------\n" + serialized);
    validate(protocol, serialized);

    inStream = new ByteArrayInputStream(serialized.getBytes());
    final TXMLProtocol protocol2 = createOutProtocol(factory);
    final ServiceSchema universe = thrift().schema().
      getModules().get("org_thriftee_thrift_protocol").
      getServices().get("Universe");
    thrift().processorFor(universe).process(protocol2, protocol2);

    final String response = formatXml(new String(outStream.toByteArray()));
    System.out.println("Response:\n-----------------------\n" + response);
    validate(protocol, response);

    inStream = new ByteArrayInputStream(response.getBytes());
    final TProtocol protocol3 = createOutProtocol(factory);
    
    TMessage rmsg = protocol3.readMessageBegin();
    assertEquals(TMessageType.REPLY, rmsg.type);

   // TStruct result = 
    protocol3.readStructBegin();
    //assertEquals("grok_result", result.name);

    TField rfield = protocol3.readFieldBegin();
    //assertEquals("success", rfield.name);
    assertEquals(TType.I32, rfield.type);

    int answer = protocol3.readI32();
    assertEquals(42, answer);
    protocol3.readFieldEnd();
    
    TField stop = protocol3.readFieldBegin();
    assertEquals(TType.STOP, stop.type);

    protocol3.readStructEnd();
    protocol3.readMessageEnd();

  }

  public <T> void testRoundtrip(
      Class<T> cl, T o, TXMLProtocol.Factory pf) throws Exception {
    TXMLProtocol protocol = createOutProtocol(factory);
    thrift().codecManager().write(cl, o, protocol);

    final String serialized = formatXml(new String(outStream.toByteArray()));
    System.out.println("Serialized:\n-----------------------\n" + serialized);
    validate(protocol, serialized);

    inStream = new ByteArrayInputStream(serialized.getBytes());
    protocol = createOutProtocol(factory);
    T roundtrip = thrift().codecManager().read(cl, protocol);
    thrift().codecManager().write(cl, roundtrip, protocol);
    
    final String rounded = formatXml(new String(outStream.toByteArray()));
    System.out.println("Round Trip:\n-----------------------\n" + rounded);
    validate(protocol, rounded);

    assertEquals(
      "result of first and second serialization should be identical", 
      o, roundtrip
    );  
  }

  public TXMLProtocol createOutProtocol(TXMLProtocol.Factory protocolFactory) {
    outStream = new ByteArrayOutputStream();
    TTransport transport = new TIOStreamTransport(inStream, outStream);
    return (TXMLProtocol) protocolFactory.getProtocol(transport);
  }

  public Everything everythingStruct() {
    Everything everything = new Everything();
    everything.bite = 42;
    everything.int32 = 64000;
    everything.int16 = 1024;
    everything.int64 = 10000000000L;
    everything.str = "foobar";
    everything.dbl = 10.4;
    everything.bin = "secret_password".getBytes();

    final Map<String, String> str_str_map = new HashMap<String, String>();
    str_str_map.put("foo", "bar");
    str_str_map.put("graffle", "florp");
    everything.str_str_map = str_str_map;

    final List<String> str_list = new ArrayList<String>();
    str_list.add("wibble");
    str_list.add("snork");
    str_list.add("spiffle");
    everything.str_list = str_list;

    final List<Spinkle> enum_list = new ArrayList<Spinkle>();
    enum_list.add(Spinkle.HRRR);
    enum_list.add(Spinkle.REWT);
    everything.enum_list = enum_list;

    final List<Sparkle> obj_list = new ArrayList<Sparkle>();
    obj_list.add(new Sparkle("blat", 17, Spinkle.HRRR));
    obj_list.add(new Sparkle("yarp", 89, Spinkle.REWT));
    obj_list.add(new Sparkle("trop", 9, null));
    everything.obj_list = obj_list;

    final Map<Integer, Sparkle> int_obj_map = new LinkedHashMap<>();
    for (int i = 0, c = obj_list.size(); i < c; i++) {
      int_obj_map.put(i + 1, obj_list.get(i));
    }
    everything.int_obj_map = int_obj_map;

    everything.obj = obj_list.get(0);
    everything.obj_set = new LinkedHashSet<>(obj_list);
    everything.str_set = new LinkedHashSet<>(str_list);

    final List<List<Integer>> int_list_list = new ArrayList<>();
    int_list_list.add(Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 }));
    int_list_list.add(Arrays.asList(new Integer[] { 1, 1, 3, 5 }));
//    everything.int_list_list = int_list_list;

    return everything;
  }

  public Office testStruct1() {
    Office struct = new Office();
    struct.setOfficeCode("OFFICE1");
    struct.setAddressLine1("Address Line 1");
    struct.setCity("Somecity");
    struct.setPhone("555-555-5555");
    struct.setTerritory("NE");
    struct.setPostalCode("12345");
    struct.setCountry("US");
    struct.setState("NY");
    return struct;
  }

  public Order testStruct2() {
    Order order = new Order();
    order.setOrderNumber(1);
    order.setCustomer(testStruct3());
    order.setComments("Test comments");
    order.setStatus("Shipped");
    order.setShippedDate(new Date());
    order.setOrderDate(new Date());
    order.setRequiredDate(new Date());
    order.setOrderDetails(new ArrayList<>());
    {
      OrderDetailPK orderDetailId1 = new OrderDetailPK();
      orderDetailId1.setOrderNumber(1);
      orderDetailId1.setProductCode("P1");
      OrderDetail orderDetail1 = new OrderDetail();
      orderDetail1.setId(orderDetailId1);
      orderDetail1.setPriceEach(150);
      orderDetail1.setQuantityOrdered(1);
      orderDetail1.setOrderLineNumber((short)1);
      order.addOrderDetail(orderDetail1);
    }{
      OrderDetailPK orderDetailId2 = new OrderDetailPK();
      orderDetailId2.setOrderNumber(1);
      orderDetailId2.setProductCode("P2");
      OrderDetail orderDetail2 = new OrderDetail();
      orderDetail2.setId(orderDetailId2);
      orderDetail2.setPriceEach(100);
      orderDetail2.setQuantityOrdered(2);
      orderDetail2.setOrderLineNumber((short)2);
      order.addOrderDetail(orderDetail2);
    }
    return order;
  }

  public Customer testStruct3() {
    Customer customer = new Customer();
    customer.setCustomerName("Joe Public");
    customer.setCustomerNumber(100);
    customer.setAddressLine1("Address Line 1");
    customer.setCity("Somecity");
    customer.setPhone("555-555-5555");
    customer.setPostalCode("12345");
    customer.setCountry("US");
    customer.setState("NY");
    customer.setContactFirstName("Jane");
    customer.setContactLastName("Public");
    customer.setCreditLimit(200);
    return customer;
  }

  public String formatXml(String s) {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StreamResult result = new StreamResult(new StringWriter());
      StreamSource source = new StreamSource(new StringReader(s));
      transformer.transform(source, result);
      String xmlString = result.getWriter().toString();
      return xmlString;
    } catch (RuntimeException e) {
      System.out.println(s);
      throw e;
    } catch (Exception e) {
      System.out.println(s);
      throw new RuntimeException(e);
    }
  }

}
