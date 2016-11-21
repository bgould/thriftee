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

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftUnion;
import com.facebook.swift.codec.ThriftUnionId;

@ThriftUnion
public class Sprat {

  private short type;

  private Object value;

  public static final short THRIFT_ID_WOOBIE = 1;

  public static final short THRIFT_ID_WOWZER = 2;

  public static final short THRIFT_ID_WHEEE = 3;

  @ThriftUnionId
  public short getType() {
    return type;
  }

  @ThriftField(THRIFT_ID_WOOBIE)
  public String getWoobie() {
    validateType(THRIFT_ID_WOOBIE);
    return (String) value;
  }

  @ThriftField
  public void setWoobie(String woobie) {
    this.type = THRIFT_ID_WOOBIE;
    this.value = woobie;
  }

  @ThriftField(THRIFT_ID_WOWZER)
  public int getWowzer() {
    validateType(THRIFT_ID_WOWZER);
    return (Integer) value;
  }

  @ThriftField
  public void setWowzer(int wowzer) {
    this.type = THRIFT_ID_WOWZER;
    this.value = wowzer;
  }

  @ThriftField(THRIFT_ID_WHEEE)
  public Spinkle getWheee() {
    validateType(THRIFT_ID_WHEEE);
    return (Spinkle) value;
  }

  @ThriftField
  public void setWheee(Spinkle wheee) {
    this.type = THRIFT_ID_WHEEE;
    this.value = wheee;
  }

  private void validateType(short type) {
    if (type != getType()) {
      throw new IllegalStateException("expected type "+type+"; was "+getType());
    }
  }

}
