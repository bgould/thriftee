/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.examples.everything;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Everything {

  private String str;
  private long int64;
  private int int32;
  private short int16;
  private byte bite;
  private double dbl;
  private ByteBuffer bin;
  private Spinkle enu;
  private Sprat onion;
  private List<String> str_list;
  private List<Spinkle> enum_list;
  private List<Spirfle> obj_list;
  private List<List<Integer>> int_list_list;
  private Map<String, String> str_str_map;
  private Map<Integer, String> int_str_map;
  private Map<Integer, Spirfle> int_obj_map;
  private Spirfle obj;
  private Set<String> str_set;
  private Set<Spirfle> obj_set;
  private Blotto smork;
  private Map<Spinkle, List<Spirfle>> enum_list_map;
  private boolean really;
  private String empty;
  private int someint;
  private Sprat someobj;
  private Blotto someobj2;

  @ThriftField(1)
  public String getStr() {
    return str;
  }

  @ThriftField
  public void setStr(String str) {
    this.str = str;
  }

  @ThriftField(2)
  public long getInt64() {
    return int64;
  }

  @ThriftField
  public void setInt64(long int64) {
    this.int64 = int64;
  }

  @ThriftField(3)
  public int getInt32() {
    return int32;
  }

  @ThriftField
  public void setInt32(int int32) {
    this.int32 = int32;
  }

  @ThriftField(4)
  public short getInt16() {
    return int16;
  }

  @ThriftField
  public void setInt16(short int16) {
    this.int16 = int16;
  }

  @ThriftField(5)
  public byte getBite() {
    return bite;
  }

  @ThriftField
  public void setBite(byte bite) {
    this.bite = bite;
  }

  @ThriftField(6)
  public double getDbl() {
    return dbl;
  }

  @ThriftField
  public void setDbl(double dbl) {
    this.dbl = dbl;
  }

  @ThriftField(7)
  public ByteBuffer getBin() {
    return bin;
  }

  @ThriftField
  public void setBin(ByteBuffer bin) {
    this.bin = bin;
  }

  @ThriftField(8)
  public Spinkle getEnu() {
    return enu;
  }

  @ThriftField
  public void setEnu(Spinkle enu) {
    this.enu = enu;
  }

  @ThriftField(9)
  public Sprat getOnion() {
    return onion;
  }

  @ThriftField
  public void setOnion(Sprat onion) {
    this.onion = onion;
  }

  @ThriftField(10)
  public List<String> getStr_list() {
    return str_list;
  }

  @ThriftField
  public void setStr_list(List<String> str_list) {
    this.str_list = str_list;
  }

  @ThriftField(11)
  public List<Spinkle> getEnum_list() {
    return enum_list;
  }

  @ThriftField
  public void setEnum_list(List<Spinkle> enum_list) {
    this.enum_list = enum_list;
  }

  @ThriftField(12)
  public List<Spirfle> getObj_list() {
    return obj_list;
  }

  @ThriftField
  public void setObj_list(List<Spirfle> obj_list) {
    this.obj_list = obj_list;
  }

  @ThriftField(13)
  public List<List<Integer>> getInt_list_list() {
    return int_list_list;
  }

  @ThriftField
  public void setInt_list_list(List<List<Integer>> int_list_list) {
    this.int_list_list = int_list_list;
  }

  @ThriftField(14)
  public Map<String, String> getStr_str_map() {
    return str_str_map;
  }

  @ThriftField
  public void setStr_str_map(Map<String, String> str_str_map) {
    this.str_str_map = str_str_map;
  }

  @ThriftField(15)
  public Map<Integer, String> getInt_str_map() {
    return int_str_map;
  }

  @ThriftField
  public void setInt_str_map(Map<Integer, String> int_str_map) {
    this.int_str_map = int_str_map;
  }

  @ThriftField(16)
  public Map<Integer, Spirfle> getInt_obj_map() {
    return int_obj_map;
  }

  @ThriftField
  public void setInt_obj_map(Map<Integer, Spirfle> int_obj_map) {
    this.int_obj_map = int_obj_map;
  }

  @ThriftField(17)
  public Spirfle getObj() {
    return obj;
  }

  @ThriftField
  public void setObj(Spirfle obj) {
    this.obj = obj;
  }

  @ThriftField(18)
  public Set<String> getStr_set() {
    return str_set;
  }

  @ThriftField
  public void setStr_set(Set<String> str_set) {
    this.str_set = str_set;
  }

  @ThriftField(19)
  public Set<Spirfle> getObj_set() {
    return obj_set;
  }

  @ThriftField
  public void setObj_set(Set<Spirfle> obj_set) {
    this.obj_set = obj_set;
  }

  @ThriftField(20)
  public Blotto getSmork() {
    return smork;
  }

  @ThriftField
  public void setSmork(Blotto smork) {
    this.smork = smork;
  }

  @ThriftField(21)
  public Map<Spinkle, List<Spirfle>> getEnum_list_map() {
    return enum_list_map;
  }

  @ThriftField
  public void setEnum_list_map(Map<Spinkle, List<Spirfle>> enum_list_map) {
    this.enum_list_map = enum_list_map;
  }

  @ThriftField(22)
  public boolean isReally() {
    return really;
  }

  @ThriftField
  public void setReally(boolean really) {
    this.really = really;
  }

  @ThriftField(23)
  public String getEmpty() {
    return empty;
  }

  @ThriftField
  public void setEmpty(String empty) {
    this.empty = empty;
  }

  @ThriftField(24)
  public int getSomeint() {
    return someint;
  }

  @ThriftField
  public void setSomeint(int someint) {
    this.someint = someint;
  }

  @ThriftField(25)
  public Sprat getSomeobj() {
    return someobj;
  }

  @ThriftField
  public void setSomeobj(Sprat someobj) {
    this.someobj = someobj;
  }

  @ThriftField(26)
  public Blotto getSomeobj2() {
    return someobj2;
  }

  @ThriftField
  public void setSomeobj2(Blotto someobj2) {
    this.someobj2 = someobj2;
  }

}
