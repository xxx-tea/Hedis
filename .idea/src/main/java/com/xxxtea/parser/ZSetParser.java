package com.xxxtea.parser;

import com.xxxtea.annotation.Command;
import com.xxxtea.common.HedisException;
import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisData;
import com.xxxtea.model.HedisZSet;

import java.util.TreeMap;

public class ZSetParser extends AbstractParser {
	DataManager dataManager = DataManager.INSTANCE;

	@Command
	public int zadd(String[] parts) {
		if (parts.length < 4 || parts.length % 2 != 0) {
			throw new HedisException("Wrong number of arguments for 'zadd' command");
		}

		String key = parts[1];
		HedisData hedisData = dataManager.get(key);
		HedisZSet hedisZSet;
		if (hedisData == null) {
			hedisZSet = new HedisZSet();
		} else {
			hedisZSet = (HedisZSet) hedisData;
		}

		for (int i = 2; i < parts.length; i += 2) {
			double score = Double.parseDouble(parts[i]);
			String member = parts[i + 1];
			hedisZSet.add(score, member);
		}

		dataManager.put(key, hedisZSet);
		return parts.length / 2 - 1;
	}

	@Command
	public String zrange(String[] parts) {
		if (parts.length != 4) {
			throw new HedisException("Wrong number of arguments for 'zrange' command");
		}

		String key = parts[1];
		int start = Integer.parseInt(parts[2]);
		int end = Integer.parseInt(parts[3]);

		HedisZSet HedisZSet = (HedisZSet) dataManager.getOrThrow(key);
		TreeMap<Double, String> zset = HedisZSet.getAll();

		int index = 0;
		StringBuilder result = new StringBuilder();
		for (String member : zset.values()) {
			if (index >= start && index <= end) {
				result.append(member).append("\n");
			}
			index++;
		}
		return result.toString();
	}

	@Command
	public int zrem(String[] parts) {
		if (parts.length != 3) {
			throw new HedisException("Wrong number of arguments for 'zrem' command");
		}

		String key = parts[1];
		String member = parts[2];

		HedisZSet hedisZSet = (HedisZSet) dataManager.getOrThrow(key);
		TreeMap<Double, String> treeMap = hedisZSet.getAll();

		Double scoreToRemove = null;
		for (Double score : treeMap.keySet()) {
			if (treeMap.get(score).equals(member)) {
				scoreToRemove = score;
				break;
			}
		}

		if (scoreToRemove != null) {
			hedisZSet.remove(scoreToRemove);
			return 1;
		} else {
			return 0;
		}
	}
}
