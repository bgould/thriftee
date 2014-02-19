/*
 * Shamelessly Plagiarized from:
 * http://code.google.com/p/jmesa/source/browse/trunk/jmesaWeb/src/org/jmesaweb/domain/President.java
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
package org.thriftee.examples.presidents;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * @since 2.0
 * @author Jeff Johnston
 */
@ThriftStruct
@Entity
public class President implements Serializable {

	private static final long serialVersionUID = 6994035590089522066L;

	private int id;
    private Name name;
    private String term;
    private Date born;
    private Date died;
    private String education;
    private String career;
    private String politicalParty;
    private String selected;

    @ThriftField(1)
    @Id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ThriftField(2)
    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    @Temporal(TemporalType.DATE)
    public Date getBorn() {
        return born;
    }

    public void setBorn(Date born) {
        this.born = born;
    }
    
    @ThriftField(3)
    public String getDateOfBirth() {
    	if (born != null)
    		return born.toString();
    	else
    		return null;
    }

    @ThriftField(4)
    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    @Temporal(TemporalType.DATE)
    public Date getDied() {
        return died;
    }

    public void setDied(Date died) {
        this.died = died;
    }
    
    @ThriftField(5)
    public String getDateOfDeath() {
    	if (died != null)
    		return died.toString();
    	else
    		return null;
    }

    @ThriftField(6)
    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    @ThriftField(7)
    public String getPoliticalParty() {
        return politicalParty;
    }

    public void setPoliticalParty(String politicalParty) {
        this.politicalParty = politicalParty;
    }

    @ThriftField(8)
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
   
    @ThriftField(9)
    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
