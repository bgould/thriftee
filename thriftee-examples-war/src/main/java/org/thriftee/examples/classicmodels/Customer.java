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
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the Customers database table.
 * 
 */
@Entity
@Table(name="Customers")
@NamedQuery(name="Customer.findAll", query="SELECT c FROM Customer c")
@ThriftStruct
public class Customer implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private int customerNumber;

  private String addressLine1;

  private String addressLine2;

  private String city;

  private String contactFirstName;

  private String contactLastName;

  private String country;

  private double creditLimit;

  private String customerName;

  private String phone;

  private String postalCode;

  private String state;

  //bi-directional many-to-one association to Employee
  @ManyToOne
  @JoinColumn(name="salesRepEmployeeNumber")
  private Employee employee;

  //bi-directional many-to-one association to Payment
  @OneToMany(mappedBy="customer")
  private List<Payment> payments;

  //bi-directional many-to-one association to Order
  @OneToMany(mappedBy="customer")
  private List<Order> orders;

  public Customer() {
  }

  @ThriftField(1)
  public int getCustomerNumber() {
    return this.customerNumber;
  }
  
  @ThriftField
  public void setCustomerNumber(int customerNumber) {
    this.customerNumber = customerNumber;
  }

  @ThriftField(3)
  public String getAddressLine1() {
    return this.addressLine1;
  }

  @ThriftField
  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  @ThriftField(4)
  public String getAddressLine2() {
    return this.addressLine2;
  }
  
  @ThriftField
  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  @ThriftField(5)
  public String getCity() {
    return this.city;
  }

  @ThriftField
  public void setCity(String city) {
    this.city = city;
  }

  @ThriftField(6)
  public String getContactFirstName() {
    return this.contactFirstName;
  }

  @ThriftField
  public void setContactFirstName(String contactFirstName) {
    this.contactFirstName = contactFirstName;
  }

  @ThriftField(7)
  public String getContactLastName() {
    return this.contactLastName;
  }

  @ThriftField
  public void setContactLastName(String contactLastName) {
    this.contactLastName = contactLastName;
  }

  @ThriftField(8)
  public String getCountry() {
    return this.country;
  }

  @ThriftField
  public void setCountry(String country) {
    this.country = country;
  }

  @ThriftField(12)
  public double getCreditLimit() {
    return this.creditLimit;
  }

  @ThriftField
  public void setCreditLimit(double creditLimit) {
    this.creditLimit = creditLimit;
  }

  @ThriftField(2)
  public String getCustomerName() {
    return this.customerName;
  }

  @ThriftField
  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  @ThriftField(9)
  public String getPhone() {
    return this.phone;
  }

  @ThriftField
  public void setPhone(String phone) {
    this.phone = phone;
  }

  @ThriftField(10)
  public String getPostalCode() {
    return this.postalCode;
  }

  @ThriftField
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  @ThriftField(11)
  public String getState() {
    return this.state;
  }

  @ThriftField
  public void setState(String state) {
    this.state = state;
  }

  @ThriftField(value=13, name="salesRep")
  public Employee getEmployee() {
    return this.employee;
  }

  @ThriftField
  public void setEmployee(Employee employee) {
    this.employee = employee;
  }

  public List<Payment> getPayments() {
    return this.payments;
  }

  public void setPayments(List<Payment> payments) {
    this.payments = payments;
  }

  public Payment addPayment(Payment payment) {
    getPayments().add(payment);
    payment.setCustomer(this);

    return payment;
  }

  public Payment removePayment(Payment payment) {
    getPayments().remove(payment);
    payment.setCustomer(null);

    return payment;
  }

  public List<Order> getOrders() {
    return this.orders;
  }

  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  public Order addOrder(Order order) {
    getOrders().add(order);
    order.setCustomer(this);

    return order;
  }

  public Order removeOrder(Order order) {
    getOrders().remove(order);
    order.setCustomer(null);

    return order;
  }

}
