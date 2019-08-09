package com.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;


public class FlexJsonUtil {

	public static <T> T fromJson(String json, Class<?> cls) {
		return new JSONDeserializer<T>().use(null, cls).deserialize(json);
	}
	
	public static <K, V> Map<K, V> fromJson(String json) {
		Map<K, V> map = new HashMap<K, V>();
		return new JSONDeserializer<Map<K, V>>().use(null, map.getClass()).deserialize(json);
	}
	
	
	public static <K, V> HashMap<K, V> fromHJson(String json) {
		HashMap<K, V> map = new HashMap<K, V>();
		return new JSONDeserializer<HashMap<K, V>>().use(null, map.getClass()).deserialize(json);
	}
	
	/**@注释：扩充Map  */
	public static <K, V> LinkedHashMap<K, V> fromJsonLink(String json) {
		LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
		return new JSONDeserializer<LinkedHashMap<K, V>>().use(null, map.getClass()).deserialize(json);
	}
	
	public String toJson() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}
	
	public static String toJson(Object obj) {
		return new JSONSerializer().exclude("*.class").serialize(obj);
	}

	public static <T> String toJsonArray(Collection<?> collection) {
		return new JSONSerializer().exclude("*.class").serialize(collection);
	}

	public static <T> List<T> fromJsonArray(String json, Class<?> cls) {
		return new JSONDeserializer<List<T>>().use(null, ArrayList.class).use("values", cls)
				.deserialize(json);
	}
}
