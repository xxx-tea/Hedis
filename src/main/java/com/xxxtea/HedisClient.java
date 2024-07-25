package com.xxxtea;

import cn.hutool.core.io.unit.DataSize;
import cn.hutool.core.thread.ThreadUtil;
import com.xxxtea.common.Constants;
import com.xxxtea.handler.ConsoleHandler;
import com.xxxtea.netty.SimpleBootstrap;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class HedisClient {
	SimpleBootstrap bootstrap;

	public static void main(String[] args) throws Exception {
		HedisClient client = new HedisClient("localhost", 6379);
		client.connect();
		ThreadUtil.safeSleep(200);
		Scanner input = new Scanner(System.in);
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

	public HedisClient(String host, int port) {
		bootstrap = new SimpleBootstrap(host, port);
		bootstrap.initialize(p -> {
			p.addLast(new LengthFieldBasedFrameDecoder((int) DataSize.ofKilobytes(64).toBytes(),
					0, 2, 0, 2));
			p.addLast(new LengthFieldPrepender(2));
			p.addLast(new StringDecoder());
			p.addLast(new StringEncoder());
			p.addLast(new ConsoleHandler());
		});
	}

	public void connect() {
		bootstrap.connect(() -> System.out.println("connect to hedis successfully"),
				() -> System.out.println("Try Reconnected..."));
	}

	public void stop() {
		bootstrap.stop();
	}

	public void sendCommand(String command) {
		bootstrap.getChannelFuture().channel().writeAndFlush(command).syncUninterruptibly();
	}
}