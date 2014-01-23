package org.thriftee.util;

import java.io.UnsupportedEncodingException;

public class Strings {

	public Strings() {
		// TODO Auto-generated constructor stub
	}

	public static final byte[] utf8Bytes(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF8 apparently is not supported.");
		}
	}
	
}
