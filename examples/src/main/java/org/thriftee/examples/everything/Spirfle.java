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
public class Spirfle {

  private String giffle;
  private int flar;
  private Spinkle spinkle;
  private int spoot;
  private Sprat sprat;
  private Blotto blotto;

  @ThriftField(1)
  public String getGiffle() {
    return giffle;
  }

  @ThriftField
  public void setGiffle(String giffle) {
    this.giffle = giffle;
  }

  @ThriftField(2)
  public int getFlar() {
    return flar;
  }

  @ThriftField
  public void setFlar(int flar) {
    this.flar = flar;
  }

  @ThriftField(3)
  public Spinkle getSpinkle() {
    return spinkle;
  }

  @ThriftField
  public void setSpinkle(Spinkle spinkle) {
    this.spinkle = spinkle;
  }

  @ThriftField(4)
  public int getSpoot() {
    return spoot;
  }

  @ThriftField
  public void setSpoot(int spoot) {
    this.spoot = spoot;
  }

  @ThriftField(5)
  public Sprat getSprat() {
    return sprat;
  }

  @ThriftField
  public void setSprat(Sprat sprat) {
    this.sprat = sprat;
  }

  @ThriftField(6)
  public Blotto getBlotto() {
    return blotto;
  }

  @ThriftField
  public void setBlotto(Blotto blotto) {
    this.blotto = blotto;
  }

}
