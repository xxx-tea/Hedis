package com.xxxtea.util;

import com.xxxtea.base.BaseTestCase;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.model.HedisData;
import com.xxxtea.serializer.KryoSerializer;

import java.io.File;
import java.util.List;
import java.util.Map;

public class KryoSerializerTest extends BaseTestCase {
	File file = new File(getHedisConfig().getRDB().getPath());

	public void testMap() throws Exception {
		Map<String, HedisData> map = KryoSerializer.readObject(file);
		System.out.println(map.size());
		byte[] bytes = KryoSerializer.writeObject(map);
		System.out.println(bytes.length);
		map = KryoSerializer.readObject(bytes);
		System.out.println(map.size());
	}

	public void testQueue() throws Exception {
		List<String> list = CommandManager.INSTANCE.getCommandList();
		System.out.println(list);
		byte[] bytes = KryoSerializer.writeObject(list);
		List<String> o = KryoSerializer.readObject(bytes);
//		System.out.println(o);
	}

}