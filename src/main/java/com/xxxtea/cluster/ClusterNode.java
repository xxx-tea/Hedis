package com.xxxtea.cluster;

import cn.hutool.core.util.StrUtil;
import com.xxxtea.common.Constants;
import lombok.Data;

import java.net.InetSocketAddress;
import java.util.List;

@Data
public class ClusterNode {
	private final String host;
	private final Integer port;
	private ClusterRole clusterRole;
	private InetSocketAddress address;
	private boolean online;
	private int offset;

	public ClusterNode(String address) {
		List<String> list = StrUtil.split(address, ":");
		this.host = list.get(0);
		this.port = Integer.parseInt(list.get(1));
		this.address = new InetSocketAddress(host, port + Constants.CLUSTER_PORT_OFFSET);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClusterNode that = (ClusterNode) o;

		if (!host.equals(that.host)) return false;
		return port.equals(that.port);
	}

	@Override
	public int hashCode() {
		int result = host.hashCode();
		result = 31 * result + port.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return Constants.getAddress(host, port);
	}
}
