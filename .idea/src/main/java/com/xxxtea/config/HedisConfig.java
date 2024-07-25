package com.xxxtea.config;

import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.util.StrUtil;
import com.xxxtea.cluster.ClusterType;
import lombok.Data;

import java.util.List;

@Data
public class HedisConfig {
	public static HedisConfig INSTANCE;

	static {
		initConfig();
	}

	private String host;
	private int port;
	private int timeout;
	private String password;
	private int maxClients;
	private DataSize bigKeySize;
	private AOF AOF;
	private RDB RDB;
	private Cluster cluster;

	private static void initConfig() {
		EnhanceYaml yamlLoader = new EnhanceYaml();
		yamlLoader.addCustomPathConvert("cluster.type", value -> ClusterType.valueOf(value.toUpperCase()));
		yamlLoader.addCustomConvert(DataSize::parse, DataSize.class);
		INSTANCE = yamlLoader.load("hedis.yaml", HedisConfig.class);

		String port = System.getenv("port");
		if (StrUtil.isNotBlank(port)) {
			INSTANCE.setPort(Integer.parseInt(port));
		}
		String rdbPath = System.getenv("RDB.path");
		if (StrUtil.isNotBlank(rdbPath)) {
			INSTANCE.getRDB().setPath(rdbPath);
		}
	}

	@Data
	public static class AOF {
		private boolean enable;
		private String path;
	}

	@Data
	public static class RDB {
		private boolean enable;
		private String path;
	}

	@Data
	public static class Cluster {
		private boolean enable;
		private ClusterType type;
		private List<String> masterNodes;
		private List<String> slaveNodes;
	}
}
