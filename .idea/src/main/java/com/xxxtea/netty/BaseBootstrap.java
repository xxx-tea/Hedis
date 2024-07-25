//package com.xxxtea.netty;
//
//import io.netty.bootstrap.AbstractBootstrap;
//import io.netty.channel.*;
//import lombok.Getter;
//
//import java.util.concurrent.TimeUnit;
//import java.util.function.Consumer;
//
//public abstract class BaseBootstrap {
//	private String host;
//	private int port;
//	private AbstractBootstrap bootstrap;
//	@Getter
//	private EventLoopGroup workerGroup;
//	@Getter
//	private ChannelFuture channelFuture;
//
//	abstract void initialize(Consumer<ChannelPipeline> pipelineConsumer);
//
//	protected void option(ChannelOption<Object> option, Object value) {
//		bootstrap.option(option, value);
//	}
//
//	protected void connect(Runnable success, Runnable failed) {
//		this.channelFuture = bootstrap.bind(host, port);
//		this.channelFuture.addListener(future -> {
//			if (future.isSuccess()) {
//				success.run();
//				return;
//			}
//
//			EventLoop eventLoop = channelFuture.channel().eventLoop();
//			eventLoop.schedule(() -> {
//				failed.run();
//				connect(success, failed);
//			}, 3, TimeUnit.SECONDS);
//		});
//	}
//
//	protected void stop() {
//		channelFuture.channel().closeFuture().syncUninterruptibly();
//		workerGroup.shutdownGracefully();
//	}
//}
