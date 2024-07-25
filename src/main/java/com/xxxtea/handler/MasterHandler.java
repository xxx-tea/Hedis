package com.xxxtea.handler;

import cn.hutool.core.collection.ListUtil;
import com.xxxtea.cluster.ClusterNode;
import com.xxxtea.cluster.ClusterRequest;
import com.xxxtea.common.Constants;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.manager.ClusterManager;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.manager.DataManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MasterHandler extends SimpleChannelInboundHandler<ClusterRequest> {
	DataManager dataManager = DataManager.INSTANCE;
	CommandManager commandManager = CommandManager.INSTANCE;
	ClusterManager clusterManager = ClusterManager.INSTANCE;

	@Override
	protected void channelRead0(ChannelHandlerContext context, ClusterRequest request) {
		switch (request.getCommand()) {
			case Constants.PING: {
				Optional<ClusterNode> optional = clusterManager.getNode(request.getAddress());
				optional.ifPresent(e -> e.setOnline(true));
				log.info("A Slave Node Connected address: [{}]", request.getAddress());
				context.channel().writeAndFlush(Constants.PONG);
				break;
			}
			case Constants.FULL_SYNC: {
				zeroCopyRDBFile(context);
//				context.channel().writeAndFlush(dataManager.getMap());
				break;
			}
			case Constants.PART_SYNC: {
				List<String> commandList = commandManager.getCommandList();
				Integer offset = (Integer) request.getData();
				List<String> offsetList = ListUtil.sub(commandList, offset, commandList.size() - 1);

				if (!offsetList.isEmpty()) {
					context.channel().writeAndFlush(offsetList);
					// 服务端更新offset
					Optional<ClusterNode> optional = clusterManager.getNode(request.getAddress());
					optional.ifPresent(e -> e.setOffset(offset));
				}

//				List<ClusterNode> slaveNodes = clusterManager.getSlaveNodes();
//				List<String> msg = slaveNodes.stream()
//						.map(e -> e.getAddress() + ",syncCount:" + e.getSyncCount()).collect(Collectors.toList());
//				System.out.println(msg);
				break;
			}
		}
	}

	private void zeroCopyRDBFile(ChannelHandlerContext context) {
		String path = HedisConfig.INSTANCE.getRDB().getPath();
		try {
			@Cleanup RandomAccessFile raf = new RandomAccessFile(path, "r");
			ChannelFuture channelFuture = context.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));
			channelFuture.addListener(future -> {
				if (future.isSuccess()) {
					System.out.println("zeroCopyRDBFile!");
				} else {
					System.out.println("zeroCopyRDBFile error!");
					future.cause().printStackTrace();
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
