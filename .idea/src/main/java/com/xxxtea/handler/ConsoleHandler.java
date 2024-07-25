package com.xxxtea.handler;

import com.google.common.base.Throwables;
import com.xxxtea.common.Constants;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
@ChannelHandler.Sharable
public class ConsoleHandler extends SimpleChannelInboundHandler<String> {

	@Override
	protected void channelRead0(ChannelHandlerContext context, String msg) {
		System.out.println(msg);
		System.out.print(Constants.CONSOLE_TIP);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(Throwables.getStackTraceAsString(cause));
		ctx.close();
	}

}
