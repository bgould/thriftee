package org.thriftee.examples.classicmodels.services;

import org.thriftee.examples.classicmodels.Order;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface OrderService {

  @ThriftMethod
  public Order findOrderById(
    @ThriftField(name="orderNumber") int orderNumber
  );

}
