package org.thriftee.util;

import java.io.UnsupportedEncodingException;

public class Strings {

    private Strings() {
    }

    public static final byte[] utf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF8 apparently is not supported.");
        }
    }
    
    public static boolean isBlank(String s) {
        return (s == null) ? true : s.trim().equals("");
    }
    
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

}
