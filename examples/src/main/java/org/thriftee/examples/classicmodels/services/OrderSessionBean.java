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
package org.thriftee.examples.classicmodels.services;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.examples.classicmodels.Order;

@Stateless
@Local(OrderService.class)
public class OrderSessionBean implements OrderService {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @PersistenceContext
  private EntityManager em;

  @Override
  public Order findOrderById(int orderNumber) {
    final Order order = em.find(Order.class, orderNumber);
//    LOG.debug("retrieved order.");
    if (order == null) {
      return null;
    }
//    if (order.getCustomer() != null) {
//      LOG.debug("also retrieved customer number: {}",
//        order.getCustomer().getCustomerNumber());
//    } else {
//      LOG.debug("customer was null");
//    }
//    if (order.getOrderDetails() != null) {
//      LOG.debug(
//        "also retrieved order details: {}",
//        order.getOrderDetails().size()
//      );
//    } else {
//      LOG.debug("order details were null");
//    }
    return order;
  }

}
