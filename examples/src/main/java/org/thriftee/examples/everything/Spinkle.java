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

import com.facebook.swift.codec.ThriftEnum;
import com.facebook.swift.codec.ThriftEnumValue;

@ThriftEnum
public enum Spinkle {

  HRRR(0),
  PPOL(2),
  REWT(3);

  private final int value;

  private Spinkle(int value) {
    this.value = value;
  }

  @ThriftEnumValue
  public int getValue() {
    return value;
  }

  public static Spinkle findByValue(int value) {
    switch (value) {
      case 0:
        return HRRR;
      case 2:
        return PPOL;
      case 3:
        return REWT;
      default:
        return null;
    }
  }
}
