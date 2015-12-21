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

import javax.persistence.Embeddable;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * The primary key class for the OrderDetails database table.
 * 
 */
@Embeddable
@ThriftStruct("OrderDetailKey")
public class OrderDetailPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private int orderNumber;

	private String productCode;

	public OrderDetailPK() {
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
  public String getProductCode() {
		return this.productCode;
	}

  @ThriftField
  public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof OrderDetailPK)) {
			return false;
		}
		OrderDetailPK castOther = (OrderDetailPK)other;
		return 
			(this.orderNumber == castOther.orderNumber)
			&& this.productCode.equals(castOther.productCode);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.orderNumber;
		hash = hash * prime + this.productCode.hashCode();
		
		return hash;
	}
}
