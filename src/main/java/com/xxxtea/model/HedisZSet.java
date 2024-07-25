package com.xxxtea.model;

import lombok.Getter;

import java.util.TreeMap;

@Getter
public class HedisZSet extends HedisData {
	private final TreeMap<Double, String> treeMap = new TreeMap<>();

	public void add(double score, String member) {
		treeMap.put(score, member);
	}

	public String get(double score) {
		return treeMap.get(score);
	}

	public void remove(double score) {
		treeMap.remove(score);
	}

	public TreeMap<Double, String> getAll() {
		return treeMap;
	}
}
