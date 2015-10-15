package org.thriftee.examples.protocols;

import java.util.Map;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Everything {

  @ThriftField(1)
  public String str;

  @ThriftField(2)
  public Long int64;

  @ThriftField(3)
  public Integer int32;

  @ThriftField(4)
  public Short int16;

  @ThriftField(5)
  public Byte bite;

  @ThriftField(6)
  public Double dbl;

  @ThriftField(7)
  public byte[] bin;

  @ThriftField(8)
  public Map<String, String> str_str_map;

  @ThriftField(9)
  public Map<Integer, String> int32_str_map;

  @ThriftEnum
  public static enum Spinkle {
    HRRR,
    PPOL,
    REWT;
  }

  @ThriftStruct
  public static class SomeKey {

    public String giffle;

    public int flar;

    public Spinkle spinkle;

  }

}
