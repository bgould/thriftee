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
package org.thriftee.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public enum New {

	SINGLETON;
	
	public static <K, V> Map<K, V> map() {
		return new HashMap<K, V>();
	}
	
	public static <K, V> SortedMap<K, V> sortedMap() {
		return new TreeMap<K, V>();
	}
	
	public static <K, V> Map<K, V> orderedMap() {
		return new LinkedHashMap<K, V>();
	}
	
	public static <T> Set<T> set() {
		return new HashSet<T>();
	}
	
	public static <T> SortedSet<T> sortedSet() {
		return new TreeSet<T>();
	}
	
	public static <T> List<T> arrayList() {
		return new ArrayList<T>();
	}
	
	public static <T> List<T> linkedList() {
		return new LinkedList<T>();
	}

}
