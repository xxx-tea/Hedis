package com.xxxtea.common;

public interface Constants {
	String OK = "OK";
	String EMPTY = "Empty";
	String PING = "ping";
	String CLUSTER_NOT_OPEN = "cluster is not open";
	String FULL_SYNC = "fsync";
	String PART_SYNC = "psync";
	String PONG = "pong";
	String NULL = "null";
	String KEY_NOT_EXIST = "key is not exist";
	String TYPE_NOT_MATCH = "type is not match";

	int CLUSTER_PORT_OFFSET = 10000;
	String CONSOLE_TIP = "Hedis> ";

	static String getOnlineCommand(String host, Integer port) {
		return PING + "@" + host + ":" + port;
	}

	static String getAddress(String host, Integer port) {
		return host + ":" + port;
	}
}
