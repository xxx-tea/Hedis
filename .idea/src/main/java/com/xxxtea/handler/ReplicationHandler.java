package com.xxxtea.handler;

import com.google.common.base.Throwables;
import com.xxxtea.common.Instance;
import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Queue;

/**
 * 从节点接收主节点的数据
 */
@Slf4j
@SuppressWarnings("unchecked")
public class ReplicationHandler extends SimpleChannelInboundHandler<Object> {
	DataManager dataManager = DataManager.INSTANCE;

	@Override
	protected void channelRead0(ChannelHandlerContext context, Object obj) {
		if (obj instanceof Map) {
			Map<String, HedisData> map = (Map<String, HedisData>) obj;
			dataManager.setMap(map);
			log.info("first sync replicate {} keys", map.size());
		} else if (obj instanceof Queue) {
			Queue<String> queue = (Queue<String>) obj;
			log.info("ready to replicate {} commands", queue.size());
			for (String command : queue) {
				Instance.PARSERLIST.stream().anyMatch(e -> e.doParse(command));
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error(Throwables.getStackTraceAsString(cause));
		ctx.close();
	}
}
