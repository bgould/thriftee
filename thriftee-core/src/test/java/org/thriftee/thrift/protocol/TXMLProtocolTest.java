package org.thriftee.thrift.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.junit.Test;
import org.thriftee.examples.classicmodels.Customer;
import org.thriftee.examples.classicmodels.Office;
import org.thriftee.examples.classicmodels.Order;
import org.thriftee.examples.classicmodels.OrderDetail;
import org.thriftee.examples.classicmodels.OrderDetailPK;
import org.thriftee.tests.AbstractThriftEETest;

public class TXMLProtocolTest extends AbstractThriftEETest {

  private ByteArrayInputStream inStream;

  private ByteArrayOutputStream outStream;

  private static final TProtocolFactory factory = new TXMLProtocol.Factory();

  //@Test
  public void testXmlParse() throws Exception {
    TProtocol protocol = createOutProtocol(factory);
    thrift().codecManager().write(Order.class, testStruct2(), protocol);

    final String serialized = formatXml(new String(outStream.toByteArray()));
    System.out.println(serialized);

    XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(
      new ByteArrayInputStream(serialized.getBytes()));
    for (int etype = reader.getEventType(); reader.hasNext(); ) {
      String typename = TXMLProtocol.XML.streamEventToString(etype);
      String desc = "";
      if (etype == XMLStreamConstants.START_ELEMENT) {
        desc = " '" + reader.getLocalName() + "'";
      } else if (etype == XMLStreamConstants.END_ELEMENT) {
        desc = " '" + reader.getLocalName() + "'";
      } else if (etype == XMLStreamConstants.CHARACTERS) {
        String text = reader.getText();
        if (!"".equals(text.trim())) {
          desc = " '" + text + "'";
        }
      }
      System.out.printf("event: %s%s (%s, %s)%n", typename, desc, etype, reader.hasNext());
      if (reader.hasNext()) {
       etype = reader.next();
      }
    }
  }
  
  //@Test
  public void testRoundTrip1() throws Exception {
    testRoundtrip(Office.class, testStruct1(), factory);
  }

  @Test
  public void testRoundTrip2() throws Exception {
    testRoundtrip(Order.class, testStruct2(), factory);
  }

  public <T> void testRoundtrip(
      Class<T> cl, T o, TProtocolFactory pf) throws Exception {
    TProtocol protocol = createOutProtocol(pf);
    thrift().codecManager().write(cl, o, protocol);

    final String serialized = formatXml(new String(outStream.toByteArray()));
    // final String serialized = new String(outStream.toByteArray());
    System.out.println(serialized);

    inStream = new ByteArrayInputStream(serialized.getBytes());
    protocol = createOutProtocol(pf);
    T roundtrip = thrift().codecManager().read(cl, protocol);
    thrift().codecManager().write(cl, roundtrip, protocol);
    
    final String rounded = formatXml(new String(outStream.toByteArray()));
    //final String rounded = new String(outStream.toByteArray());
    System.out.println(rounded);
  }

  public TProtocol createOutProtocol(TProtocolFactory protocolFactory) {
    outStream = new ByteArrayOutputStream();
    TTransport transport = new TIOStreamTransport(inStream, outStream);
    return protocolFactory.getProtocol(transport);
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
