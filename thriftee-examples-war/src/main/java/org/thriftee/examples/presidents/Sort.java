package org.thriftee.examples.presidents;

import java.io.Serializable;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Sort implements Serializable {

	private static final long serialVersionUID = 2189350344482034085L;

	private final String property;
	private final SortOrder order;

	@ThriftConstructor
	public Sort(String property, SortOrder order) {
		this.property = property;
		this.order = order;
	}

	@ThriftField(1)
	public String getProperty() {
		return property;
	}

	@ThriftField(2)
	public SortOrder getOrder() {
		return order;
	}
}
