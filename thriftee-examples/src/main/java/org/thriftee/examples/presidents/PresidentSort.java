package org.thriftee.examples.presidents;

/*
 * Shamelessly plagiarized from:
 * http://code.google.com/p/jmesa/source/browse/trunk/jmesaWeb/src/org/jmesaweb/dao/PresidentSort.java
 *
 * Copyright 2004 original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Creates a command to wrap the Hibernate criteria API to sort.
 *
 * @since 2.0
 * @author Jeff Johnston
 */
//@ThriftStruct
public class PresidentSort implements CriteriaCommand {
    
	private List<Sort> sorts = new ArrayList<Sort>();

    public void addSort(String property, SortOrder order) {
        sorts.add(new Sort(property, order));
    }
    
    @ThriftField(1)
    public void setSorts(List<Sort> sorts) {
    	if (sorts == null) {
    		throw new IllegalArgumentException("The list of sorts cannot be null");
    	}
    	this.sorts = sorts;
    }
    
    public List<Sort> getSorts() {
    	return this.sorts;
    }

    public void execute(CriteriaBuilder criteria, CriteriaQuery<?> query) {
    	List<Order> orders = new LinkedList<Order>();
    	Root<President> president = query.from(President.class);
        for (Sort sort : sorts) {
        	switch (sort.getOrder()) {
        	case DESC:
        		orders.add(criteria.desc(president.get(sort.getProperty())));
        		break;
        	case ASC:
    		default:
    			orders.add(criteria.asc(president.get(sort.getProperty())));
        	}
        }
        if (orders.size() > 0) {
        	Order[] ordersArray = orders.toArray(new Order[orders.size()]);
        	query.orderBy(ordersArray);
        }
    }

}
