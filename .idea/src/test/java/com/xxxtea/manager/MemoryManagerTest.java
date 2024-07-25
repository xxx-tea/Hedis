package com.xxxtea.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.xxxtea.common.Instance;
import com.xxxtea.model.HedisString;
import junit.framework.TestCase;

import java.util.concurrent.CopyOnWriteArrayList;

public class MemoryManagerTest extends TestCase {

	public void testGetUsedMemory() {
		DataManager dataManager = DataManager.INSTANCE;
		for (int i = 0; i < 100000; i++) {
			String key = RandomUtil.randomString(RandomUtil.randomInt(3, 20));
			String value = RandomUtil.randomString(RandomUtil.randomInt(2, 500));
			HedisString hedisData = new HedisString(value);
			hedisData.setAccessTime(System.currentTimeMillis() + RandomUtil.randomLong(-100000, 100000));
			dataManager.put(key, hedisData);
		}
		long usedMemory = MemoryManager.INSTANCE.getUsedMemory();
		Instance.TIMER.start();
		Instance.TIMER.log(() -> MemoryManager.INSTANCE.clear(), "");
		System.out.println(usedMemory + "," + FileUtil.readableFileSize(usedMemory));
	}


}