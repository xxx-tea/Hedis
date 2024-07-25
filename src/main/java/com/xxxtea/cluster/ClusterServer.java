package com.xxxtea.cluster;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.unit.DataSize;
import com.xxxtea.coder.KryoDecoder;
import com.xxxtea.coder.KryoEncoder;
import com.xxxtea.common.Constants;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.handler.MasterHandler;
import com.xxxtea.manager.ClusterManager;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.netty.SimpleServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

/**
 * 集群通信点
 */
@Slf4j
public class ClusterServer {
	SimpleServerBootstrap serverBootstrap;
	HedisConfig hedisConfig = HedisConfig.INSTANCE;

	public ClusterServer() {
		serverBootstrap = new SimpleServerBootstrap(hedisConfig.getHost(), hedisConfig.getPort() + Constants.CLUSTER_PORT_OFFSET);
		serverBootstrap.initialize(p -> {
			p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofMegabytes(1).toBytes(), 0, 4, 0, 4));
			p.addLast(new LengthFieldPrepender(4));
			p.addLast(new KryoDecoder());
			p.addLast(new KryoEncoder());
			p.addLast(new MasterHandler());
		});
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 20);
	}

	public void start() {
		serverBootstrap.start();
		log.info("cluster server start [{}]", serverBootstrap.getChannelFuture().channel().localAddress());
//		serverBootstrap.getWorkerGroup().scheduleAtFixedRate(() -> {
//			ClusterManager.INSTANCE.getSlaveNodes().stream()
//					.mapToInt(ClusterNode::getOffset)
//					.min().ifPresent(offset -> {
//						List<String> commandList = CommandManager.INSTANCE.getCommandList();
//						if (offset > 1) {
//							commandList.subList(0, offset - 1).clear();
//						}
//					});
//		}, 1, 1, TimeUnit.MINUTES);
	}

}
