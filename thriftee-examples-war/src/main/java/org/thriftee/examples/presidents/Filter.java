package org.thriftee.examples.presidents;

import java.io.Serializable;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Filter implements Serializable {

	private static final long serialVersionUID = -247166099068174692L;

	private final String property;
	private final String value;

	@ThriftConstructor
	public Filter(String property, String value) {
		this.property = property;
		this.value = value;
	}

	@ThriftField(1)
	public String getProperty() {
		return property;
	}

	@ThriftField(2)
	public String getValue() {
		return value;
	}
}
