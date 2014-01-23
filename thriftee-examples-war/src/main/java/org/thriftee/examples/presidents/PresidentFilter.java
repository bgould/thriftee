package org.thriftee.examples.presidents;

/*
 * Shameless plagiarized from:
 * http://code.google.com/p/jmesa/source/browse/trunk/jmesaWeb/src/org/jmesaweb/dao/PresidentFilter.java
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
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.thriftee.util.New;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Creates a command to wrap the Hibernate criteria API to filter.
 *
 * @since 2.0
 * @author Jeff Johnston
 */
@ThriftStruct
public class PresidentFilter implements CriteriaCommand {
    
	private List<Filter> filters = new ArrayList<Filter>();

    public void addFilter(String property, String value) {
        filters.add(new Filter(property, value));
    }

    public void execute(CriteriaBuilder cb, CriteriaQuery<President> criteria) {
    	Root<President> president = criteria.from(President.class);
    	criteria.select(president);
    	List<Predicate> predicates = New.linkedList();
        for (Filter filter : filters) {
        	if (filter.getValue() != null) {
        		String property = (String) filter.getProperty();
        		String value = filter.getValue().toString();
        		predicates.add(
        			cb.like(
						cb.upper(president.get(property).as(String.class)), 
						cb.upper(cb.literal("%" + value + "%"))
					)
				);
        	}
        }
        if (predicates.size() > 0) {
        	Predicate[] predicateArray = predicates.toArray(new Predicate[predicates.size()]);
        	criteria.where(predicateArray);
        }
    }
    
    @ThriftField(1)
    public List<Filter> getFilters() {
    	return filters;
    }
    
    @ThriftField
    public void setFilters(List<Filter> filters) {
    	if (filters == null) {
    		throw new IllegalArgumentException("Filter list cannot be null");
    	}
    	this.filters = filters;
    }

}
