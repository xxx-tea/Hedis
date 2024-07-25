package com.xxxtea.parser;

import com.xxxtea.annotation.Command;
import com.xxxtea.common.Constants;
import com.xxxtea.common.HedisException;
import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisString;

import java.util.ArrayList;
import java.util.List;

public class StringParser extends AbstractParser {
	DataManager dataManager = DataManager.INSTANCE;

	@Command(aof = true)
	public String set(String[] parts) throws HedisException {
		if (parts.length != 3) {
			throw new HedisException("Wrong number of arguments for 'set' command");
		}
		String key = parts[1];
		String value = parts[2];
		dataManager.put(key, new HedisString(value));
		return Constants.OK;
	}

	@Command
	public HedisString get(String[] parts) throws HedisException {
		if (parts.length != 2) {
			throw new HedisException("Wrong number of arguments for 'get' command");
		}

		String key = parts[1];
		HedisString hedisString = dataManager.getOrThrow(key);
		hedisString.incrHitCount();
		return hedisString;
	}

	@Command(aof = true)
	public int incr(String[] parts) throws HedisException {
		if (parts.length != 2) {
			throw new HedisException("Wrong number of arguments for 'incr' command");
		}

		String key = parts[1];
		HedisString hedisString = dataManager.get(key);
		return hedisString.incr();
	}

	@Command(aof = true)
	public int decr(String[] parts) throws HedisException {
		if (parts.length != 2) {
			throw new HedisException("Wrong number of arguments for 'decr' command");
		}

		String key = parts[1];
		HedisString hedisString = dataManager.get(key);
		return hedisString.decr();
	}

	@Command
	public List<HedisString> mget(String[] parts) throws HedisException {
		if (parts.length < 2) {
			throw new HedisException("Wrong number of arguments for 'mget' command");
		}

		List<HedisString> result = new ArrayList<>();
		for (int i = 1; i < parts.length; i++) {
			String key = parts[i];
			HedisString hedisString = dataManager.get(key);
			result.add(hedisString);
		}
		return result;
	}
}

