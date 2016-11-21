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
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public class Blotto {

  private int rimple;
  private String sparticle;

  @ThriftField(1)
  public int getRimple() {
    return rimple;
  }

  @ThriftField
  public void setRimple(int rimple) {
    this.rimple = rimple;
  }

  @ThriftField(2)
  public String getSparticle() {
    return sparticle;
  }

  @ThriftField
  public void setSparticle(String sparticle) {
    this.sparticle = sparticle;
  }

}
