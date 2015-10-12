package org.thriftee.examples.classicmodels;

import static org.thriftee.examples.classicmodels.Order.dateFromString;
import static org.thriftee.examples.classicmodels.Order.dateToString;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the Payments database table.
 */
@Entity
@Table(name="Payments")
@NamedQuery(name="Payment.findAll", query="SELECT p FROM Payment p")
@ThriftStruct
public class Payment implements Serializable {

  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private PaymentPK id;

  private double amount;

  @Temporal(TemporalType.TIMESTAMP)
  private Date paymentDate;

  //bi-directional many-to-one association to Customer
  @ManyToOne
  @JoinColumn(name="customerNumber", insertable=false, updatable=false)
  private Customer customer;

  public Payment() {
  }

  @ThriftField(1)
  public PaymentPK getId() {
    return this.id;
  }

  @ThriftField
  public void setId(PaymentPK id) {
    this.id = id;
  }

  @ThriftField(2)
  public double getAmount() {
    return this.amount;
  }

  @ThriftField
  public void setAmount(double amount) {
    this.amount = amount;
  }

  public Date getPaymentDate() {
    return this.paymentDate;
  }

  public void setPaymentDate(Date paymentDate) {
    this.paymentDate = paymentDate;
  }

  @ThriftField(name="paymentDate", value=3)
  public String getPaymentDateString() {
    return dateToString(getPaymentDate());
  }

  @ThriftField
  public void setPaymentDateString(String paymentDate) {
    setPaymentDate(dateFromString(paymentDate));
  }

  public Customer getCustomer() {
    return this.customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  @ThriftField(value=4)
  public Integer getCustomerId() {
    if (getCustomer() == null) {
      return null;
    } else {
      return getCustomer().getCustomerNumber();
    }
  }

}
