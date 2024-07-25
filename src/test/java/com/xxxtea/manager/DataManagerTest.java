package com.xxxtea.manager;

import cn.hutool.core.util.RandomUtil;
import com.xxxtea.base.BaseTestCase;
import com.xxxtea.model.HedisString;

public class DataManagerTest extends BaseTestCase {

	public void testOfferUpdateData() {
		DataManager dataManager = getDataManager();
		for (int i = 0; i < 10000; i++) {
			String key = RandomUtil.randomString(RandomUtil.randomInt(3, 20));
			String value = RandomUtil.randomString(RandomUtil.randomInt(2, 500));
			dataManager.put(key, new HedisString(value));
		}
		System.out.println(MemoryManager.INSTANCE.getUsedMemory());
	}

}