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
	private final InetSocketAddress address;
	private boolean online;

	public ClusterNode(String address) {
		List<String> list = StrUtil.split(address, ":");
		this.host = list.get(0);
		this.port = Integer.parseInt(list.get(1));
		this.address = new InetSocketAddress(host, port + Constants.CLUSTER_PORT_OFFSET);
	}

}
