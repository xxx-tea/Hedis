package com.xxxtea;

import cn.hutool.core.thread.ThreadUtil;
import com.xxxtea.handler.ConsoleHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HedisClient {
	private final String host;
	private final int port;
	private final ConsoleHandler resultHandler = new ConsoleHandler();
	private EventLoopGroup workerGroup;
	private Bootstrap bootstrap;
	private ChannelFuture channelFuture;

	public HedisClient(String host, int port) {
		this.host = host;
		this.port = port;
		initializeBootstrap();
	}

	public static void main(String[] args) throws Exception {
		HedisClient client = new HedisClient("localhost", 6379);
		client.connect();

		Scanner input = new Scanner(System.in);
		ThreadUtil.safeSleep(20);
		while (true) {
			System.out.print("Hedis> ");
			String command = input.nextLine();
			if (command.equalsIgnoreCase("exit")) {
				break;
			}
			client.channelFuture.channel().writeAndFlush("").syncUninterruptibly();
			CompletableFuture<String> result = client.sendCommand(command);
			System.out.println(result.get(3, TimeUnit.SECONDS));
		}
		client.stop();
	}

	private void initializeBootstrap() {
		workerGroup = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(workerGroup)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new ChannelInboundHandlerAdapter() {
							@Override
							public void channelInactive(ChannelHandlerContext ctx) {
								connect();
							}
						});
						p.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
						p.addLast(new LengthFieldPrepender(2));
						p.addLast(new StringDecoder());
						p.addLast(new StringEncoder());
						p.addLast(resultHandler);
					}
				});
	}

	public void connect() {
		try {
			channelFuture = bootstrap.connect(host, port);
			channelFuture.addListener(future -> {
				if (future.isSuccess()) {
					System.out.println("connect to hedis successfully");
				} else {
					channelFuture.channel().eventLoop().schedule(() -> {
						try {
							System.out.println("Try Reconnected...");
							channelFuture = bootstrap.connect(host, port);
						} catch (Exception ignored) {
						}
					}, 3, TimeUnit.SECONDS);
				}
			});
		} catch (Exception e) {
			workerGroup.shutdownGracefully();
			throw e;
		}
	}

	public void stop() throws Exception {
		try {
			channelFuture.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
		}
	}

	public CompletableFuture<String> sendCommand(String command) {
		CompletableFuture<String> resultFuture = new CompletableFuture<>();
		resultHandler.setResultFuture(resultFuture);
		channelFuture.channel().writeAndFlush(command).syncUninterruptibly();
		return resultFuture;
	}
}