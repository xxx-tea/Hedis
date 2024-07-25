package com.xxxtea.manager;

import com.xxxtea.cluster.ClusterNode;
import com.xxxtea.cluster.ClusterRole;
import com.xxxtea.config.HedisConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
public class ClusterManager {
	public static ClusterManager INSTANCE = new ClusterManager();
	HedisConfig hedisConfig = HedisConfig.INSTANCE;
	private List<ClusterNode> masterNodes;
	private List<ClusterNode> slaveNodes;
	private ClusterNode currentClusterNode;
	private boolean enable = true;
	@Setter
	private boolean firstSync = true;

	private ClusterManager() {
		this.init();
	}

	private void init() {
		HedisConfig.Cluster cluster = hedisConfig.getCluster();
		if (cluster == null || !cluster.isEnable()) {
			enable = false;
			return;
		}

		this.masterNodes = new ArrayList<>();
		for (String address : cluster.getMasterNodes()) {
			ClusterNode clusterNode = new ClusterNode(address);
			clusterNode.setClusterRole(ClusterRole.MASTER);
			this.masterNodes.add(clusterNode);
			if (hedisConfig.getAddress().equals(clusterNode.toString())) {
				this.currentClusterNode = clusterNode;
			}
		}

		this.slaveNodes = new ArrayList<>();
		for (String address : cluster.getSlaveNodes()) {
			ClusterNode clusterNode = new ClusterNode(address);
			clusterNode.setClusterRole(ClusterRole.SLAVE);
			this.slaveNodes.add(clusterNode);
			if (hedisConfig.getAddress().equals(clusterNode.toString())) {
				this.currentClusterNode = clusterNode;
			}
		}
	}

	public Optional<ClusterNode> getNode(String address) {
		return slaveNodes.stream().filter(e -> e.toString().equals(address)).findAny();
	}

}
