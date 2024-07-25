package com.xxxtea;

import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.thread.ThreadUtil;
import com.xxxtea.common.Constants;
import com.xxxtea.handler.ConsoleHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class HedisClient {
	private final String host;
	private final int port;
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
		System.out.print(Constants.CONSOLE_TIP);
		while (true) {
			String command = input.nextLine();
			if (command.equalsIgnoreCase("exit")) {
				break;
			}
			client.sendCommand(command);
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
						p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofKilobytes(64).toBytes(),
								0, 2, 0, 2));
						p.addLast(new LengthFieldPrepender(2));
						p.addLast(new StringDecoder());
						p.addLast(new StringEncoder());
						p.addLast(new ConsoleHandler());
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

	public void sendCommand(String command) {
		channelFuture.channel().writeAndFlush(command).syncUninterruptibly();
	}
}