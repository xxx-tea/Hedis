package com.xxxtea.manager;

import com.xxxtea.cluster.ClusterNode;
import com.xxxtea.cluster.ClusterType;
import com.xxxtea.config.HedisConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class ClusterManager {
	public static ClusterManager INSTANCE = new ClusterManager();
	HedisConfig hedisConfig = HedisConfig.INSTANCE;
	private ClusterType clusterType;
	private List<ClusterNode> masterNodes;
	private List<ClusterNode> slaveNodes;
	@Setter
	private boolean firstSync = true;

	private ClusterManager() {
		this.init();
	}

	private void init() {
		HedisConfig.Cluster cluster = hedisConfig.getCluster();
		if (cluster == null) {
			return;
		}

//		this.clusterType = ClusterType.valueOf(hedisConfig.getCluster().getType().toUpperCase());
//		try {
//		} catch (IllegalArgumentException e) {
//			log.error("Config 'clusterType' must be in [master_slave, multi_master_slave]");
//			System.exit(0);
//		}
		this.masterNodes = cluster.getMasterNodes().stream().map(ClusterNode::new).collect(Collectors.toList());
		this.slaveNodes = cluster.getSlaveNodes().stream().map(ClusterNode::new).collect(Collectors.toList());
	}

	public boolean isMaster() {
		Optional<ClusterNode> optional = this.masterNodes.stream().filter(e -> e.getHost().equals(hedisConfig.getHost())
				&& e.getPort() == hedisConfig.getPort()).findAny();
		return optional.isPresent();
	}

}
