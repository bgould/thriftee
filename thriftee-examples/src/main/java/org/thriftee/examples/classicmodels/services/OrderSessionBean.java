package org.thriftee.examples.classicmodels.services;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thriftee.examples.classicmodels.Order;

@Stateless
@Remote(OrderService.class)
public class OrderSessionBean implements OrderService {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @PersistenceContext
  private EntityManager em;

  @Override
  public Order findOrderById(int orderNumber) {
    final Order order = em.find(Order.class, orderNumber);
    LOG.debug("retrieved order.");
    if (order == null) {
      return null;
    }
    if (order.getCustomer() != null) {
      LOG.debug("also retrieved customer number: {}", 
        order.getCustomer().getCustomerNumber());
    } else {
      LOG.debug("customer was null");
    }
    if (order.getOrderDetails() != null) {
      LOG.debug(
        "also retrieved order details: {}", 
        order.getOrderDetails().size()
      );
    } else {
      LOG.debug("order details were null");
    }
    return order;
  }

}
