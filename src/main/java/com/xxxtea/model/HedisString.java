package com.xxxtea.model;

import lombok.Getter;

@Getter
public class HedisString extends HedisData {
	private String value;

	public HedisString(String value) {
		this.value = value;
	}

	public int incr() {
		int intValue = Integer.parseInt(value) + 1;
		this.value = String.valueOf(intValue);
		return intValue;
	}

	public int decr() {
		int intValue = Integer.parseInt(value) - 1;
		this.value = String.valueOf(intValue);
		return intValue;
	}

	@Override
	public String toString() {
		return value;
	}
}
