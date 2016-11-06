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
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the Offices database table.
 * 
 */
@Entity
@Table(name="Offices")
@NamedQuery(name="Office.findAll", query="SELECT o FROM Office o")
@ThriftStruct
public class Office implements Serializable {
  
  private static final long serialVersionUID = 1L;

  @Id
  private String officeCode;

  private String addressLine1;

  private String addressLine2;

  private String city;

  private String country;

  private String phone;

  private String postalCode;

  private String state;

  private String territory;

  //bi-directional many-to-one association to Employee
  @OneToMany(mappedBy="office")
  private List<Employee> employees;

  public Office() {
  }

  @ThriftField(1)
  public String getOfficeCode() {
    return this.officeCode;
  }

  @ThriftField
  public void setOfficeCode(String officeCode) {
    this.officeCode = officeCode;
  }

  @ThriftField(2)
  public String getAddressLine1() {
    return this.addressLine1;
  }

  @ThriftField
  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  @ThriftField(3)
  public String getAddressLine2() {
    return this.addressLine2;
  }

  @ThriftField
  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  @ThriftField(4)
  public String getCity() {
    return this.city;
  }

  @ThriftField
  public void setCity(String city) {
    this.city = city;
  }

  @ThriftField(7)
  public String getCountry() {
    return this.country;
  }

  @ThriftField
  public void setCountry(String country) {
    this.country = country;
  }

  @ThriftField(8)
  public String getPhone() {
    return this.phone;
  }

  @ThriftField
  public void setPhone(String phone) {
    this.phone = phone;
  }

  @ThriftField(6)
  public String getPostalCode() {
    return this.postalCode;
  }

  @ThriftField
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  @ThriftField(5)
  public String getState() {
    return this.state;
  }

  @ThriftField
  public void setState(String state) {
    this.state = state;
  }

  @ThriftField(9)
  public String getTerritory() {
    return this.territory;
  }

  @ThriftField
  public void setTerritory(String territory) {
    this.territory = territory;
  }

  public List<Employee> getEmployees() {
    return this.employees;
  }

  public void setEmployees(List<Employee> employees) {
    this.employees = employees;
  }

  public Employee addEmployee(Employee employee) {
    getEmployees().add(employee);
    employee.setOffice(this);

    return employee;
  }

  public Employee removeEmployee(Employee employee) {
    getEmployees().remove(employee);
    employee.setOffice(null);

    return employee;
  }

}