package com.xxxtea.cluster;

import cn.hutool.core.io.unit.DataSize;
import com.xxxtea.coder.KryoDecoder;
import com.xxxtea.coder.KryoEncoder;
import com.xxxtea.common.Constants;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.handler.ReplicationHandler;
import com.xxxtea.manager.ClusterManager;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.manager.DataManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * 集群服务器
 */
@Slf4j
public class ClusterServer {
	HedisConfig hedisConfig = HedisConfig.INSTANCE;
	DataManager dataManager = DataManager.INSTANCE;
	ClusterManager clusterManager = ClusterManager.INSTANCE;
	CommandManager commandManager = CommandManager.INSTANCE;

	public ClusterServer() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofMegabytes(1).toBytes(),
									0, 4, 0, 4));
							p.addLast(new LengthFieldPrepender(4));
							p.addLast(new KryoDecoder());
							p.addLast(new KryoEncoder());
							p.addLast(new ReplicationHandler());
						}
					});

			ChannelFuture future = serverBootstrap.bind(hedisConfig.getHost(), hedisConfig.getPort() + Constants.CLUSTER_PORT_OFFSET).sync();
			if (clusterManager.isMaster()) {
				this.connectToAllNodes();
			}
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private void connectToAllNodes() {
		for (ClusterNode node : clusterManager.getSlaveNodes()) {
			ClusterBootstrap clusterBootstrap = new ClusterBootstrap(node);
			clusterBootstrap.connect();
		}
	}

	class ClusterBootstrap {
		private final ClusterNode node;
		private Bootstrap bootstrap;
		private ChannelFuture channelFuture;

		public ClusterBootstrap(ClusterNode node) {
			this.node = node;
			this.init();
		}

		public void init() {
			bootstrap = new Bootstrap();
			NioEventLoopGroup workerGroup = new NioEventLoopGroup();
			workerGroup.scheduleAtFixedRate(() -> {
				// 主节点向从节点发送数据
				if (clusterManager.isMaster() && channelFuture != null) {
					Queue<String> queue = commandManager.getCommandQueue();
					if (clusterManager.isFirstSync()) {
						clusterManager.setFirstSync(false);
						channelFuture.channel().writeAndFlush(dataManager.getMap());
					} else if (!queue.isEmpty()) {
						channelFuture.channel().writeAndFlush(queue);
					}
				}
			}, 0, 3, TimeUnit.SECONDS);
			bootstrap.group(workerGroup)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofMegabytes(1).toBytes(),
									0, 4, 0, 4));
							p.addLast(new LengthFieldPrepender(4));
							p.addLast(new KryoDecoder());
							p.addLast(new KryoEncoder());
						}
					});
		}

		public void connect() {
			this.channelFuture = bootstrap.connect(node.getAddress());
			channelFuture.addListener(future -> {
				if (future.isSuccess()) {
					node.setOnline(true);
					log.info("Connected to cluster node: [{}:{}]", node.getHost(), node.getPort());
					return;
				}

				node.setOnline(false);
				channelFuture.channel().eventLoop().schedule(() -> {
					log.info("Try Reconnected...");
					connect();
				}, 3, TimeUnit.SECONDS);

				log.error("Failed to connect to cluster node: [{}:{}]", node.getHost(), node.getPort());
			});
		}
	}

}
