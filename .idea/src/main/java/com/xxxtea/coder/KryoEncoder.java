package com.xxxtea.coder;

import com.xxxtea.serializer.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class KryoEncoder extends MessageToByteEncoder<Object> {
	@Override
	protected void encode(ChannelHandlerContext context, Object obj, ByteBuf byteBuf) throws Exception {
		byte[] bytes = KryoSerializer.writeObject(obj);
		byteBuf.writeBytes(bytes);
	}
}
