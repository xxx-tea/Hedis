package com.xxxtea.handler;

import com.google.common.base.Throwables;
import com.xxxtea.cluster.ClusterNode;
import com.xxxtea.common.Instance;
import com.xxxtea.manager.ClusterManager;
import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 从节点接收主节点的数据
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ReplicationHandler extends SimpleChannelInboundHandler<Object> {
	DataManager dataManager = DataManager.INSTANCE;
	ClusterManager clusterManager = ClusterManager.INSTANCE;

	@Override
	protected void channelRead0(ChannelHandlerContext context, Object obj) {
		if (obj instanceof DefaultFileRegion) {
			DefaultFileRegion fileRegion = (DefaultFileRegion) obj;
		} else if (obj instanceof Map) {
			Map<String, HedisData> map = (Map<String, HedisData>) obj;
			dataManager.setMap(map);
			log.info("first sync {} keys", map.size());
		} else if (obj instanceof List) {
			List<String> list = (List<String>) obj;
			for (String command : list) {
				boolean success = Instance.PARSERLIST.stream().anyMatch(e -> e.doParse(command));
				if (!success) {
					log.error("error command: [{}]", command);
				}
			}
			// 更新offset
			ClusterNode clusterNode = clusterManager.getCurrentClusterNode();
			clusterNode.setOffset(clusterNode.getOffset() + list.size());
			log.info("part sync {} commands, offset: {}", list.size(), clusterNode.getOffset());
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(Throwables.getStackTraceAsString(cause));
		ctx.close();
	}
}
