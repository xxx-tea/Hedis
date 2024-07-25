package com.xxxtea.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SimpleBootstrap {
	private final String host;
	private final int port;
	private Bootstrap bootstrap;
	@Getter
	private EventLoopGroup workerGroup;
	@Getter
	private ChannelFuture channelFuture;

	public SimpleBootstrap(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public void initialize(Consumer<ChannelPipeline> pipelineConsumer) {
		this.bootstrap = new Bootstrap();
		this.workerGroup = new NioEventLoopGroup();
		this.bootstrap.group(workerGroup)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						pipelineConsumer.accept(p);
					}
				});
	}

	public void option(ChannelOption<Object> option, Object value) {
		this.bootstrap.option(option, value);
	}

	public void connect() {
		this.channelFuture = bootstrap.connect(host, port).syncUninterruptibly();
	}

	public void connect(Runnable success, Runnable failed) {
		this.channelFuture = bootstrap.connect(host, port);
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
		try {
			channelFuture.channel().closeFuture().syncUninterruptibly();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
}
