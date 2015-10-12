package org.thriftee.examples.classicmodels;

import java.io.Serializable;

import javax.persistence.Embeddable;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * The primary key class for the Payments database table.
 * 
 */
@Embeddable
@ThriftStruct("PaymentKey")
public class PaymentPK implements Serializable {

  //default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private int customerNumber;

	private String checkNumber;

	public PaymentPK() {}

  @ThriftField(1)
	public int getCustomerNumber() {
		return this.customerNumber;
	}

  @ThriftField
  public void setCustomerNumber(int customerNumber) {
		this.customerNumber = customerNumber;
	}

  @ThriftField(2)
  public String getCheckNumber() {
		return this.checkNumber;
	}

  @ThriftField
  public void setCheckNumber(String checkNumber) {
		this.checkNumber = checkNumber;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PaymentPK)) {
			return false;
		}
		final PaymentPK castOther = (PaymentPK) other;
		return 
			(this.customerNumber == castOther.customerNumber)
			&& this.checkNumber.equals(castOther.checkNumber);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.customerNumber;
		hash = hash * prime + this.checkNumber.hashCode();
		return hash;
	}

}
