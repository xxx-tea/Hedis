package com.xxxtea.manager;

import com.xxxtea.common.Constants;
import com.xxxtea.common.HedisException;
import com.xxxtea.model.HedisData;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@SuppressWarnings("unchecked")
public class DataManager {
	public static DataManager INSTANCE = new DataManager();
	/* 存储数据的map */
	private Map<String, HedisData> map;
	private Set<String> bigKeySet;

	private DataManager() {
		map = new HashMap<>();
		bigKeySet = new HashSet<>();
	}

	public void put(String key, HedisData hedisData) {
		map.put(key, hedisData);
	}

	public void remove(String key) {
		map.remove(key);
	}

	public <T> T get(String key) {
		return (T) map.get(key);
	}

	public <T> T getOrThrow(String key) {
		HedisData hedisData = map.get(key);
		if (hedisData == null) {
			throw new HedisException(Constants.NULL);
		}
		return (T) hedisData;
	}

	public Set<String> keySet() {
		return map.keySet();
	}
}
