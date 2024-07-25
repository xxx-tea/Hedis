package com.xxxtea.handler;

import com.google.common.base.Throwables;
import com.xxxtea.common.HedisException;
import com.xxxtea.common.Instance;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.parser.AbstractParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * 命令解析处理器
 */
@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<String> {

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String command) {
		Instance.TIMER.start();

		try {
			Optional<AbstractParser> optional = Instance.PARSERLIST.stream().filter(e -> e.doParse(command)).findFirst();
			if (optional.isPresent()) {
				AbstractParser parser = optional.get();
				ctx.writeAndFlush(parser.getResult());
			} else {
				ctx.writeAndFlush("Error Command: " + command);
				return;
			}
		} catch (HedisException e) {
			ctx.writeAndFlush(e.getMessage());
		}

		log.info("Client Command: [{}] cost: [{}]", command, Instance.TIMER.intervalPretty());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(Throwables.getStackTraceAsString(cause));
		ctx.close();
	}
}
