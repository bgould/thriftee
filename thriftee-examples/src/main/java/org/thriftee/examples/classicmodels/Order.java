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
package org.thriftee.examples.classicmodels;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the Orders database table.
 * 
 */
@Entity
@Table(name="Orders")
@NamedQuery(name="Order.findAll", query="SELECT o FROM Order o")
@ThriftStruct
public class Order implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private int orderNumber;

  @Lob
  private String comments;

  @Temporal(TemporalType.TIMESTAMP)
  private Date orderDate;

  @Temporal(TemporalType.TIMESTAMP)
  private Date requiredDate;

  @Temporal(TemporalType.TIMESTAMP)
  private Date shippedDate;

  private String status;

  //bi-directional many-to-one association to Customer
  @ManyToOne
  @JoinColumn(name="customerNumber")
  private Customer customer;

  //bi-directional many-to-one association to OrderDetail
  @OneToMany(mappedBy="order")
  private List<OrderDetail> orderDetails;

  public Order() {
  }

  @ThriftField(1)
  public int getOrderNumber() {
    return this.orderNumber;
  }

  @ThriftField
  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  @ThriftField(2)
  public String getComments() {
    return this.comments;
  }

  @ThriftField
  public void setComments(String comments) {
    this.comments = comments;
  }

  public Date getOrderDate() {
    return this.orderDate;
  }

  public void setOrderDate(Date orderDate) {
    this.orderDate = orderDate;
  }

  @ThriftField(name="orderDate", value=3)
  public String getOrderDateString() {
    return dateToString(getOrderDate());
  }

  @ThriftField
  public void setOrderDateString(String orderDateString) {
    setOrderDate(dateFromString(orderDateString));
  }

  public Date getRequiredDate() {
    return this.requiredDate;
  }

  public void setRequiredDate(Date requiredDate) {
    this.requiredDate = requiredDate;
  }
  
  @ThriftField(name="requiredDate", value=4)
  public String getRequiredDateString() {
    return dateToString(getRequiredDate());
  }

  @ThriftField
  public void setRequiredDateString(String requiredDate) {
    setRequiredDate(dateFromString(requiredDate));
  }

  public Date getShippedDate() {
    return this.shippedDate;
  }

  public void setShippedDate(Date shippedDate) {
    this.shippedDate = shippedDate;
  }

  @ThriftField(name="shippedDate", value=5)
  public String getShippedDateString() {
    return dateToString(getShippedDate());
  }

  @ThriftField
  public void setShippedDateString(String shippedDate) {
    setShippedDate(dateFromString(shippedDate));
  }

  @ThriftField(value=6)
  public String getStatus() {
    return this.status;
  }

  @ThriftField
  public void setStatus(String status) {
    this.status = status;
  }

  @ThriftField(value=7)
  public Customer getCustomer() {
    return this.customer;
  }

  @ThriftField
  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  @ThriftField(value=8)
  public List<OrderDetail> getOrderDetails() {
    return this.orderDetails;
  }

  @ThriftField
  public void setOrderDetails(List<OrderDetail> orderDetails) {
    this.orderDetails = orderDetails;
  }

  public OrderDetail addOrderDetail(OrderDetail orderDetail) {
    getOrderDetails().add(orderDetail);
    orderDetail.setOrder(this);

    return orderDetail;
  }

  public OrderDetail removeOrderDetail(OrderDetail orderDetail) {
    getOrderDetails().remove(orderDetail);
    orderDetail.setOrder(null);

    return orderDetail;
  }

  public static Date dateFromString(String s) {
    if (s == null) {
      return null;
    } else {
      final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      try {
        return sdf.parse(s);
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String dateToString(Date date) {
    if (date != null) {
      return String.format("%tF", date);
    } else {
      return null; 
    }
  }

}
