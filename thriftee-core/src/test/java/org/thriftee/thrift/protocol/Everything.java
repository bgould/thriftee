package org.thriftee.thrift.protocol;

import java.util.List;
import java.util.Map;

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.ThriftUnion;
import com.facebook.swift.codec.ThriftUnionId;

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
  public Spinkle enu;

  @ThriftField(9)
  public Sprat onion;

  @ThriftField(10)
  public List<String> str_list;

  @ThriftField(11)
  public List<Spinkle> enum_list;

  @ThriftField(12)
  public List<Sparkle> obj_list;

  @ThriftField(13)
  public List<List<Integer>> int_list_list;

  @ThriftField(14)
  public Map<String, String> str_str_map;
/*
  @ThriftField(10)
  public Map<Integer, String> int32_str_map;
*/
  @ThriftEnum
  public static enum Spinkle {
    HRRR,
    PPOL,
    REWT;
  }

  @ThriftStruct
  public static class Sparkle {

    public Sparkle() {}

    public Sparkle(String giffle, int flar, Spinkle spinkle) {
      this.giffle = giffle;
      this.flar = flar;
      this.spinkle = spinkle;
    }

    @ThriftField(1)
    public String giffle;

    @ThriftField(2)
    public int flar;

    @ThriftField(3)
    public Spinkle spinkle;

  }

  @ThriftUnion
  public static class Sprat {

    private short type;
    private String woobie;
    private int wowzer;
    private Spinkle wheee;

    @ThriftUnionId
    public short getId() {
      return type;
    }

    @ThriftField(1)
    public String getWoobie() {
      return woobie;
    }

    @ThriftField
    public void setWoobie(String woobie) {
      this.woobie = woobie;
      this.type = 1;
    }

    @ThriftField(2)
    public int getWowzer() {
      return wowzer;
    }

    @ThriftField
    public void setWowzer(int wowzer) {
      this.wowzer = wowzer;
      this.type = 2;
    }

    @ThriftField(3)
    public Spinkle getWheee() {
      return wheee;
    }

    @ThriftField
    public void setWheee(Spinkle wheee) {
      this.wheee = wheee;
      this.type = 3;
    }
    
  }

}
