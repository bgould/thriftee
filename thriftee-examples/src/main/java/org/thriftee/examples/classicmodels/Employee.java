package org.thriftee.examples.classicmodels;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the Employees database table.
 * 
 */
@Entity
@Table(name="Employees")
@NamedQuery(name="Employee.findAll", query="SELECT e FROM Employee e")
@ThriftStruct
public class Employee implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private int employeeNumber;

  private String email;

  private String extension;

  private String firstName;

  private String jobTitle;

  private String lastName;

  private int reportsTo;

  //bi-directional many-to-one association to Customer
  @OneToMany(mappedBy="employee")
  private List<Customer> customers;

  //bi-directional many-to-one association to Office
  @ManyToOne
  @JoinColumn(name="officeCode")
  private Office office;

  public Employee() {
  }

  @ThriftField(1)
  public int getEmployeeNumber() {
    return this.employeeNumber;
  }

  @ThriftField
  public void setEmployeeNumber(int employeeNumber) {
    this.employeeNumber = employeeNumber;
  }

  @ThriftField(5)
  public String getEmail() {
    return this.email;
  }

  @ThriftField
  public void setEmail(String email) {
    this.email = email;
  }

  @ThriftField(6)
  public String getExtension() {
    return this.extension;
  }

  @ThriftField
  public void setExtension(String extension) {
    this.extension = extension;
  }

  @ThriftField(2)
  public String getFirstName() {
    return this.firstName;
  }

  @ThriftField
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @ThriftField(4)
  public String getJobTitle() {
    return this.jobTitle;
  }

  @ThriftField
  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  @ThriftField(3)
  public String getLastName() {
    return this.lastName;
  }

  @ThriftField
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public int getReportsTo() {
    return this.reportsTo;
  }

  public void setReportsTo(int reportsTo) {
    this.reportsTo = reportsTo;
  }

  public List<Customer> getCustomers() {
    return this.customers;
  }

  public void setCustomers(List<Customer> customers) {
    this.customers = customers;
  }

  public Customer addCustomer(Customer customer) {
    getCustomers().add(customer);
    customer.setEmployee(this);

    return customer;
  }

  public Customer removeCustomer(Customer customer) {
    getCustomers().remove(customer);
    customer.setEmployee(null);

    return customer;
  }

  public Office getOffice() {
    return this.office;
  }

  public void setOffice(Office office) {
    this.office = office;
  }

}