package org.thriftee.util;

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
