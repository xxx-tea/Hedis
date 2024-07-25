package com.xxxtea.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class SimpleServerBootstrap {
	private final String host;
	private final int port;
	private ServerBootstrap serverBootstrap;
	private EventLoopGroup bossGroup;
	@Getter
	private EventLoopGroup workerGroup;
	@Getter
	private ChannelFuture channelFuture;

	public SimpleServerBootstrap(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void initialize(Consumer<ChannelPipeline> pipelineConsumer) {
		this.serverBootstrap = new ServerBootstrap();
		this.bossGroup = new NioEventLoopGroup();
		this.workerGroup = new NioEventLoopGroup();
		this.serverBootstrap.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						pipelineConsumer.accept(p);
					}
				});
	}

	public void connect() {
		this.channelFuture = serverBootstrap.bind(host, port).syncUninterruptibly();
	}

	public void connect(Runnable success, Runnable failed) {
		this.channelFuture = serverBootstrap.bind(host, port);
		this.channelFuture.addListener(future -> {
			if (future.isSuccess()) {
				success.run();
				return;
			}

			EventLoop eventLoop = channelFuture.channel().eventLoop();
			eventLoop.schedule(() -> {
				failed.run();
				connect(success, failed);
			}, 3, TimeUnit.SECONDS);
		});
	}

	public void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

}
