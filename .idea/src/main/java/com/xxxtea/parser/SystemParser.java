package com.xxxtea.parser;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.util.DateUtils;
import com.xxxtea.annotation.Command;
import com.xxxtea.common.Constants;
import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisData;
import com.xxxtea.util.Assert;
import com.xxxtea.util.TimeHelper;

import java.util.concurrent.TimeUnit;

public class SystemParser extends AbstractParser {
	DataManager dataManager = DataManager.INSTANCE;

	@Command
	public String ping(String[] parts) {
		Assert.checkParamsCount(parts.length == 1);
		return Constants.PONG;
	}

	@Command
	public String bigkey(String[] parts) {
		Assert.checkParamsCount(parts.length == 1);
		return StrUtil.join("\n", dataManager.getBigKeySet());
	}

	@Command
	public String keys(String[] parts) {
		Assert.checkParamsCount(parts.length == 1);
		return StrUtil.join("\n", dataManager.keySet());
	}

	@Command(aof = true)
	public void flushall(String[] parts) {
		Assert.checkParamsCount(parts.length == 1);
		dataManager.getMap().clear();
	}

	@Command
	public int dbsize(String[] parts) {
		Assert.checkParamsCount(parts.length == 1);
		return dataManager.keySet().size();
	}

	@Command(aof = true)
	public String expire(String[] parts) {
		Assert.checkParamsCount(parts.length >= 3);

		String key = parts[1];
		String ttl = parts[2];
		TimeUnit timeUnit = TimeUnit.SECONDS;
		if (parts.length == 4) {
			String unit = parts[3].toUpperCase();
			timeUnit = TimeHelper.parse(unit);
		}

		HedisData hedisData = dataManager.getOrThrow(key);
		long expireTime = System.currentTimeMillis() + timeUnit.toMillis(Long.parseLong(ttl));
		hedisData.setExpireTime(expireTime);
		return DateUtils.format(hedisData.getExpireTime());
	}

	@Command
	public String ttl(String[] parts) {
		Assert.checkParamsCount(parts.length == 2);
		HedisData hedisData = dataManager.getOrThrow(parts[1]);
		return DateUtils.format(hedisData.getExpireTime());
	}
}
