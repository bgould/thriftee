package org.thriftee.thrift.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  @ThriftField(15)
  public Map<Integer, String> int_str_map;

  @ThriftField(16)
  public Map<Integer, Sparkle> int_obj_map;

  @ThriftField(17)
  public Sparkle obj;

  @ThriftField(18)
  public Set<String> str_set;

  @ThriftField(19)
  public Set<Sparkle> obj_set;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(bin);
    result = prime * result + ((bite == null) ? 0 : bite.hashCode());
    result = prime * result + ((dbl == null) ? 0 : dbl.hashCode());
    result = prime * result + ((enu == null) ? 0 : enu.hashCode());
    result = prime * result + ((enum_list == null) ? 0 : enum_list.hashCode());
    result = prime * result + ((int16 == null) ? 0 : int16.hashCode());
    result = prime * result + ((int32 == null) ? 0 : int32.hashCode());
    result = prime * result + ((int64 == null) ? 0 : int64.hashCode());
    result = prime * result
            + ((int_list_list == null) ? 0 : int_list_list.hashCode());
    result = prime * result
            + ((int_obj_map == null) ? 0 : int_obj_map.hashCode());
    result = prime * result
            + ((int_str_map == null) ? 0 : int_str_map.hashCode());
    result = prime * result + ((obj == null) ? 0 : obj.hashCode());
    result = prime * result + ((obj_list == null) ? 0 : obj_list.hashCode());
    result = prime * result + ((obj_set == null) ? 0 : obj_set.hashCode());
    result = prime * result + ((onion == null) ? 0 : onion.hashCode());
    result = prime * result + ((str == null) ? 0 : str.hashCode());
    result = prime * result + ((str_list == null) ? 0 : str_list.hashCode());
    result = prime * result + ((str_set == null) ? 0 : str_set.hashCode());
    result = prime * result
            + ((str_str_map == null) ? 0 : str_str_map.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
        return true;
    if (obj == null)
        return false;
    if (getClass() != obj.getClass())
        return false;
    Everything other = (Everything) obj;
    if (!Arrays.equals(bin, other.bin))
        return false;
    if (bite == null) {
        if (other.bite != null)
            return false;
    } else if (!bite.equals(other.bite))
        return false;
    if (dbl == null) {
        if (other.dbl != null)
            return false;
    } else if (!dbl.equals(other.dbl))
        return false;
    if (enu != other.enu)
        return false;
    if (enum_list == null) {
        if (other.enum_list != null)
            return false;
    } else if (!enum_list.equals(other.enum_list))
        return false;
    if (int16 == null) {
        if (other.int16 != null)
            return false;
    } else if (!int16.equals(other.int16))
        return false;
    if (int32 == null) {
        if (other.int32 != null)
            return false;
    } else if (!int32.equals(other.int32))
        return false;
    if (int64 == null) {
        if (other.int64 != null)
            return false;
    } else if (!int64.equals(other.int64))
        return false;
    if (int_list_list == null) {
        if (other.int_list_list != null)
            return false;
    } else if (!int_list_list.equals(other.int_list_list))
        return false;
    if (int_obj_map == null) {
        if (other.int_obj_map != null)
            return false;
    } else if (!int_obj_map.equals(other.int_obj_map))
        return false;
    if (int_str_map == null) {
        if (other.int_str_map != null)
            return false;
    } else if (!int_str_map.equals(other.int_str_map))
        return false;
    if (this.obj == null) {
        if (other.obj != null)
            return false;
    } else if (!this.obj.equals(other.obj))
        return false;
    if (obj_list == null) {
        if (other.obj_list != null)
            return false;
    } else if (!obj_list.equals(other.obj_list))
        return false;
    if (obj_set == null) {
        if (other.obj_set != null)
            return false;
    } else if (!obj_set.equals(other.obj_set))
        return false;
    if (onion == null) {
        if (other.onion != null)
            return false;
    } else if (!onion.equals(other.onion))
        return false;
    if (str == null) {
        if (other.str != null)
            return false;
    } else if (!str.equals(other.str))
        return false;
    if (str_list == null) {
        if (other.str_list != null)
            return false;
    } else if (!str_list.equals(other.str_list))
        return false;
    if (str_set == null) {
        if (other.str_set != null)
            return false;
    } else if (!str_set.equals(other.str_set))
        return false;
    if (str_str_map == null) {
        if (other.str_str_map != null)
            return false;
    } else if (!str_str_map.equals(other.str_str_map))
        return false;
    return true;
  }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + flar;
        result = prime * result + ((giffle == null) ? 0 : giffle.hashCode());
        result = prime * result + ((spinkle == null) ? 0 : spinkle.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sparkle other = (Sparkle) obj;
        if (flar != other.flar)
            return false;
        if (giffle == null) {
            if (other.giffle != null)
                return false;
        } else if (!giffle.equals(other.giffle))
            return false;
        if (spinkle != other.spinkle)
            return false;
        return true;
    }

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
