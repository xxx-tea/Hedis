package com.xxxtea.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 添加
 */
@Setter
@Getter
public abstract class HedisData {
	private int hitCount;
	private long createTime;
	private long accessTime;
	private long expireTime = -1;

	public HedisData() {
		this.createTime = this.accessTime = System.currentTimeMillis();
	}

	public boolean isExpire() {
		if (expireTime == -1) {
			return false;
		}
		return System.currentTimeMillis() > expireTime;
	}

	public void incrHitCount() {
		hitCount++;
	}

}
