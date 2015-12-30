package org.thriftee.compiler.schema;

// Adapted from Facebook's Swift tool - https://github.com/facebook/swift
public enum ThriftProtocolType
{
    UNKNOWN((byte) 0),
    BOOL((byte) 2),
    BYTE((byte) 3),
    DOUBLE((byte) 4),
    I16((byte) 6),
    I32((byte) 8),
    I64((byte) 10),
    STRING((byte) 11),
    STRUCT((byte) 12),
    MAP((byte) 13),
    SET((byte) 14),
    LIST((byte) 15),
    ENUM((byte) 8), // same as I32 type
    BINARY((byte) 11); // same as STRING type

    private final byte type;

    private ThriftProtocolType(byte type)
    {
        this.type = type;
    }

    public byte getType()
    {
        return type;
    }
}