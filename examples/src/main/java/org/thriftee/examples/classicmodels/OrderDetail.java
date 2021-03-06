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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the OrderDetails database table.
 * 
 */
@Entity
@Table(name="OrderDetails")
@NamedQuery(name="OrderDetail.findAll", query="SELECT o FROM OrderDetail o")
@ThriftStruct
public class OrderDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private OrderDetailPK id;

	private short orderLineNumber;

	private double priceEach;

	private int quantityOrdered;

	//bi-directional many-to-one association to Order
	@ManyToOne
	@JoinColumn(name="orderNumber", insertable=false, updatable=false)
	private Order order;

	//bi-directional many-to-one association to Product
	@ManyToOne
	@JoinColumn(name="productCode", insertable=false, updatable=false)
	private Product product;

	public OrderDetail() {
	}

  @ThriftField(1)
	public OrderDetailPK getId() {
		return this.id;
	}

  @ThriftField
	public void setId(OrderDetailPK id) {
		this.id = id;
	}

  @ThriftField(2)
	public short getOrderLineNumber() {
		return this.orderLineNumber;
	}

  @ThriftField
	public void setOrderLineNumber(short orderLineNumber) {
		this.orderLineNumber = orderLineNumber;
	}

  @ThriftField(3)
	public double getPriceEach() {
		return this.priceEach;
	}

  @ThriftField
	public void setPriceEach(double priceEach) {
		this.priceEach = priceEach;
	}

  @ThriftField(4)
	public int getQuantityOrdered() {
		return this.quantityOrdered;
	}

  @ThriftField
	public void setQuantityOrdered(int quantityOrdered) {
		this.quantityOrdered = quantityOrdered;
	}

	public Order getOrder() {
		return this.order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return this.product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}
