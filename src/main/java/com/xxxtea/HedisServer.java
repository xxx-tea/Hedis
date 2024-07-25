package com.xxxtea;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.thread.ThreadUtil;
import com.xxxtea.cluster.ClusterClient;
import com.xxxtea.cluster.ClusterNode;
import com.xxxtea.cluster.ClusterRole;
import com.xxxtea.cluster.ClusterServer;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.handler.CommandHandler;
import com.xxxtea.manager.ClusterManager;
import com.xxxtea.netty.SimpleServerBootstrap;
import com.xxxtea.task.CleaningExpireKeyTask;
import com.xxxtea.task.RDBStoreTask;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HedisServer {
	SimpleServerBootstrap serverBootstrap;
	HedisConfig hedisConfig = HedisConfig.INSTANCE;
	ClusterManager clusterManager = ClusterManager.INSTANCE;
	// 单线程执行命令解析
	EventExecutorGroup commandThreadGroup = new DefaultEventExecutorGroup(1);

	public HedisServer() {
		serverBootstrap = new SimpleServerBootstrap(hedisConfig.getHost(), hedisConfig.getPort());
		serverBootstrap.initialize(p -> {
			p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofKilobytes(64).toBytes(),
					0, 2, 0, 2));
			p.addLast(new LengthFieldPrepender(2));
			p.addLast(new IdleStateHandler(0, 0, 3000));
			p.addLast(new StringDecoder());
			p.addLast(new StringEncoder());
			p.addLast(commandThreadGroup, new CommandHandler());
			log.info("A Client Connected address: [{}]", p.channel().remoteAddress());
		});
		serverBootstrap.option(ChannelOption.SO_BACKLOG, hedisConfig.getMaxClients());
	}

	public static void main(String[] args) throws Exception {
		new HedisServer().start();
	}

	public void start() throws Exception {
		// 启动服务器
		serverBootstrap.start();
		// 添加定时任务
		addScheduleTask(serverBootstrap.getWorkerGroup());
		// 开启集群通信点
		if (hedisConfig.getCluster().isEnable()) {
			ThreadUtil.execute(() -> new ClusterServer().start());
		}
		// 从节点发起与主节点通信
		if (clusterManager.getCurrentClusterNode().getClusterRole() == ClusterRole.SLAVE) {
			List<ClusterNode> masterNodes = clusterManager.getMasterNodes();
			DefaultEventExecutorGroup eventExecutors = new DefaultEventExecutorGroup(masterNodes.size());
			for (ClusterNode masterNode : masterNodes) {
				eventExecutors.execute(() -> new ClusterClient(masterNode).connect());
			}
		}
		// 打印banner
		printBanner();
		// 关闭服务器再次刷盘保证数据实时
		serverBootstrap.stop(() -> new RDBStoreTask().run());
	}

	/**
	 * 添加定时任务
	 */
	private void addScheduleTask(EventLoopGroup workerGroup) {
		workerGroup.scheduleAtFixedRate(new CleaningExpireKeyTask(), 0L, 1L, TimeUnit.SECONDS);
		if (hedisConfig.getAOF().isEnable()) {
//			workerGroup.scheduleAtFixedRate(new AOFStoreTask(), 0L, 1L, TimeUnit.SECONDS);
		}
		if (hedisConfig.getRDB().isEnable()) {
			workerGroup.scheduleAtFixedRate(new RDBStoreTask(), 0L, 1L, TimeUnit.SECONDS);
		}
	}

	/**
	 * 打印banner和日志
	 */
	private void printBanner() throws IOException {
		InputStream stream = ResourceUtil.getStream("banner.txt");
		String banner = IoUtil.readUtf8(stream);
		SocketAddress localAddress = serverBootstrap.getChannelFuture().channel().localAddress();
		log.info("hedis server start [{}]\n{}", localAddress, banner);
		stream.close();
	}
}
