package org.thriftee.examples.presidents;
/*
 * Shamelessly plagiarized from:
 * http://code.google.com/p/jmesa/source/browse/trunk/jmesaWeb/src/org/jmesaweb/dao/PresidentDao.java
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

import java.util.List;
import java.util.Map;

import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

/**
 * @since 2.0
 * @author Jeff Johnston
 */
@ThriftService
public interface PresidentService {
	
	@ThriftMethod
    public List<President> getPresidents();

	@ThriftMethod
    public int getPresidentsCountWithFilter(PresidentFilter filter);

	@ThriftMethod
    public List<President> getPresidentsWithFilterAndSort(PresidentFilter filter, PresidentSort sort, int rowStart, int rowEnd);
   
	@ThriftMethod
    public Map<String, President> getPresidentsByUniqueIds(String property, List<String> uniqueIds);
   
    public void save(President president);
}
