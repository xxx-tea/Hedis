package com.xxxtea;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.xxxtea.cluster.ClusterServer;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.handler.CommandHandler;
import com.xxxtea.task.CleaningExpireKeyTask;
import com.xxxtea.task.RDBStoreTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
import java.util.concurrent.TimeUnit;

@Slf4j
public class HedisServer {
	// 单线程执行命令解析
	private final EventExecutorGroup commandThreadGroup = new DefaultEventExecutorGroup(1);
	private final EventExecutorGroup singleThreadGroup = new DefaultEventExecutorGroup(1);
	HedisConfig hedisConfig = HedisConfig.INSTANCE;

	public static void main(String[] args) throws Exception {
		new HedisServer().start();
	}

	public void start() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		addScheduleTask(workerGroup);
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, hedisConfig.getMaxClients())
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
							p.addLast(new LengthFieldPrepender(2));
							p.addLast(new IdleStateHandler(0, 0, 3000));
							p.addLast(new StringDecoder());
							p.addLast(new StringEncoder());
							p.addLast(commandThreadGroup, new CommandHandler());
							log.info("A Client Connected address: [{}]", ch.remoteAddress());
						}
					});
			ChannelFuture future = bootstrap.bind(hedisConfig.getHost(), hedisConfig.getPort()).sync();
			printBanner(future.channel().localAddress());
			// 开启集群
			if (hedisConfig.getCluster().isEnable()) {
				singleThreadGroup.execute(ClusterServer::new);
			}
			future.channel().closeFuture().sync();
		} finally {
			// 关闭服务器再次刷盘保证数据实时
			new RDBStoreTask().run();
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	/**
	 * 添加定时任务
	 */
	private void addScheduleTask(EventLoopGroup workerGroup) {
		workerGroup.scheduleAtFixedRate(new CleaningExpireKeyTask(), 0L, 1L, TimeUnit.SECONDS);
//		if (hedisConfig.getAOF().isEnable()) {
//			workerGroup.scheduleAtFixedRate(new AOFStoreTask(), 0L, 1L, TimeUnit.SECONDS);
//		}
		if (hedisConfig.getRDB().isEnable()) {
			workerGroup.scheduleAtFixedRate(new RDBStoreTask(), 0L, 1L, TimeUnit.SECONDS);
		}
	}

	/**
	 * 打印banner和日志
	 *
	 * @param socketAddress 服务端启动地址
	 */
	private void printBanner(SocketAddress socketAddress) throws IOException {
		InputStream stream = ResourceUtil.getStream("banner.txt");
		String banner = IoUtil.readUtf8(stream);
		log.info("hedis server start [{}]\n{}", socketAddress, banner);
		stream.close();
	}
}
