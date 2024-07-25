package com.xxxtea.cluster;

import cn.hutool.core.io.unit.DataSize;
import com.xxxtea.coder.KryoDecoder;
import com.xxxtea.coder.KryoEncoder;
import com.xxxtea.common.Constants;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.handler.ReplicationHandler;
import com.xxxtea.manager.ClusterManager;
import com.xxxtea.netty.SimpleBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClusterClient {
	ClusterNode masterNode;
	SimpleBootstrap bootstrap;
	ScheduledExecutorService scheduler;
	HedisConfig hedisConfig = HedisConfig.INSTANCE;
	ClusterManager clusterManager = ClusterManager.INSTANCE;

	public ClusterClient(ClusterNode masterNode) {
		this.masterNode = masterNode;
		this.scheduler = Executors.newScheduledThreadPool(1);
		bootstrap = new SimpleBootstrap(masterNode.getAddress());
		bootstrap.initialize(p -> {
			p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofMegabytes(1).toBytes(),
					0, 4, 0, 4));
			p.addLast(new LengthFieldPrepender(4));
			p.addLast(new KryoDecoder());
			p.addLast(new KryoEncoder());
			p.addLast(new ReplicationHandler());
		});
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
	}

	public void connect() {
		bootstrap.connect(() -> {
			log.info("Connected to Master node: [{}:{}]", masterNode.getHost(), masterNode.getPort());
			Channel channel = bootstrap.getChannelFuture().channel();
			// 发送上线数据
			ClusterRequest request = ClusterRequest.builder()
					.command(Constants.PING)
					.address(hedisConfig.getAddress())
					.build();
			channel.writeAndFlush(request).syncUninterruptibly();
			// 全量同步
			request = ClusterRequest.builder()
					.command(Constants.FULL_SYNC)
					.address(hedisConfig.getAddress())
					.build();
			channel.writeAndFlush(request).syncUninterruptibly();
			// 增量同步定时任务
			this.addPartSyncTask();
		}, () -> log.error("Failed to connect to Master node: [{}:{}]", masterNode.getHost(), masterNode.getPort()));
	}

	private void addPartSyncTask() {
		bootstrap.getWorkerGroup().scheduleAtFixedRate(() -> {
			ClusterRequest request = ClusterRequest.builder()
					.command(Constants.PART_SYNC)
					.address(hedisConfig.getAddress())
					.data(clusterManager.getCurrentClusterNode().getOffset())
					.build();
			bootstrap.getChannelFuture().channel().writeAndFlush(request);
		}, 1, 1, TimeUnit.SECONDS);
	}

}
