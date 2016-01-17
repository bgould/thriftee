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
package org.thriftee.examples.usergroup.domain;

import java.io.Serializable;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

@ThriftStruct
public final class User implements Serializable {

  private static final long serialVersionUID = 1L;

  private String uid;

  private String firstName;

  private String lastName;

  private String displayName;

  private String email;

  @ThriftField(1)
  public String getUid() {
    return uid;
  }

  @ThriftField
  public void setUid(String uid) {
    this.uid = uid;
  }

  @ThriftField(2)
  public String getFirstName() {
    return firstName;
  }

  @ThriftField
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @ThriftField(3)
  public String getLastName() {
    return lastName;
  }

  @ThriftField
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @ThriftField(4)
  public String getDisplayName() {
    return displayName;
  }

  @ThriftField
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @ThriftField(5)
  public String getEmail() {
    return email;
  }

  @ThriftField
  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "User [uid=" + uid + ", firstName=" + firstName + ", "
        + "lastName=" + lastName + ", displayName=" + displayName
        + ", email=" + email + "]";
  }

}
