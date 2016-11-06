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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the Products database table.
 * 
 */
@Entity
@Table(name="Products")
@NamedQuery(name="Product.findAll", query="SELECT p FROM Product p")
@ThriftStruct
public class Product implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	private String productCode;

	private double buyPrice;

	@Column(name="MSRP")
	private double msrp;

	@Lob
	private String productDescription;

	private String productName;

	private String productScale;

	private String productVendor;

	private short quantityInStock;

	//bi-directional many-to-one association to OrderDetail
	@OneToMany(mappedBy="product")
	private List<OrderDetail> orderDetails;

	//bi-directional many-to-one association to ProductLine
	@ManyToOne
	@JoinColumn(name="productLine")
	private ProductLine productLineBean;

	public Product() {
	}

  @ThriftField(1)
	public String getProductCode() {
		return this.productCode;
	}

  @ThriftField
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

  @ThriftField(2)
	public double getBuyPrice() {
		return this.buyPrice;
	}

  @ThriftField
	public void setBuyPrice(double buyPrice) {
		this.buyPrice = buyPrice;
	}

  @ThriftField(3)
	public double getMsrp() {
		return this.msrp;
	}

  @ThriftField
	public void setMsrp(double msrp) {
		this.msrp = msrp;
	}

  @ThriftField(4)
	public String getProductDescription() {
		return this.productDescription;
	}

  @ThriftField
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

  @ThriftField(5)
	public String getProductName() {
		return this.productName;
	}

  @ThriftField
	public void setProductName(String productName) {
		this.productName = productName;
	}

  @ThriftField(6)
	public String getProductScale() {
		return this.productScale;
	}

  @ThriftField
	public void setProductScale(String productScale) {
		this.productScale = productScale;
	}

  @ThriftField(7)
	public String getProductVendor() {
		return this.productVendor;
	}

  @ThriftField
	public void setProductVendor(String productVendor) {
		this.productVendor = productVendor;
	}

  @ThriftField(8)
	public short getQuantityInStock() {
		return this.quantityInStock;
	}

  @ThriftField
	public void setQuantityInStock(short quantityInStock) {
		this.quantityInStock = quantityInStock;
	}

  @ThriftField(9)
	public List<OrderDetail> getOrderDetails() {
		return this.orderDetails;
	}

  @ThriftField
	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}

	public OrderDetail addOrderDetail(OrderDetail orderDetail) {
		getOrderDetails().add(orderDetail);
		orderDetail.setProduct(this);

		return orderDetail;
	}

	public OrderDetail removeOrderDetail(OrderDetail orderDetail) {
		getOrderDetails().remove(orderDetail);
		orderDetail.setProduct(null);

		return orderDetail;
	}

  @ThriftField(11)
	public ProductLine getProductLineBean() {
		return this.productLineBean;
	}

  @ThriftField
	public void setProductLineBean(ProductLine productLineBean) {
		this.productLineBean = productLineBean;
	}

}
