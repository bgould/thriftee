package org.thriftee.examples.presidents;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
/*
 * Shamelessly plagiarized from:
 * http://code.google.com/p/jmesa/source/browse/trunk/jmesaWeb/src/org/jmesaweb/domain/Name.java
 * 
 * Copyright 2004 original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @since 2.0
 * @author Jeff Johnston
 */
@ThriftStruct
@Embeddable
public class Name implements Serializable {

	private static final long serialVersionUID = -7067244903481687841L;
	private String firstName;
    private String lastName;
    private String nickName;

    @ThriftConstructor
    public Name() {
    }

    public Name(String firstName, String lastName, String nickName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickName = nickName;
    }

    @ThriftField(1)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @ThriftField(2)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Transient
    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    @ThriftField(3)
    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
