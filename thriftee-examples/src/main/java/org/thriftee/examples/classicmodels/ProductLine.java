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
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;


/**
 * The persistent class for the ProductLines database table.
 * 
 */
@Entity
@Table(name="ProductLines")
@NamedQuery(name="ProductLine.findAll", query="SELECT p FROM ProductLine p")
@ThriftStruct
public class ProductLine implements Serializable {
  
  private static final long serialVersionUID = 1L;

  @Id
  private String productLine;

  @Lob
  private String htmlDescription;

  @Lob
  private byte[] image;

  @Column(length=4000)
  private String textDescription;

  //bi-directional many-to-one association to Product
  @OneToMany(mappedBy="productLineBean")
  private List<Product> products;

  public ProductLine() {
  }

  @ThriftField(1)
  public String getProductLine() {
    return this.productLine;
  }

  @ThriftField
  public void setProductLine(String productLine) {
    this.productLine = productLine;
  }

  @ThriftField(3)
  public String getHtmlDescription() {
    return this.htmlDescription;
  }

  @ThriftField
  public void setHtmlDescription(String htmlDescription) {
    this.htmlDescription = htmlDescription;
  }

  @ThriftField(4)
  public byte[] getImage() {
    return this.image;
  }

  @ThriftField
  public void setImage(byte[] image) {
    this.image = image;
  }

  @ThriftField(2)
  public String getTextDescription() {
    return this.textDescription;
  }

  @ThriftField
  public void setTextDescription(String textDescription) {
    this.textDescription = textDescription;
  }

  public List<Product> getProducts() {
    return this.products;
  }

  public void setProducts(List<Product> products) {
    this.products = products;
  }

  public Product addProduct(Product product) {
    getProducts().add(product);
    product.setProductLineBean(this);

    return product;
  }

  public Product removeProduct(Product product) {
    getProducts().remove(product);
    product.setProductLineBean(null);

    return product;
  }

}