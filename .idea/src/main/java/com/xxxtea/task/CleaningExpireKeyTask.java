package com.xxxtea.task;

import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisData;
import lombok.extern.slf4j.Slf4j;

/**
 * 清理过期key任务
 */
@Slf4j
public class CleaningExpireKeyTask implements Runnable {
	DataManager dataManager = DataManager.INSTANCE;

	@Override
	public void run() {
		// 迭代器安全删除
		dataManager.keySet().iterator().forEachRemaining(key -> {
			HedisData hedisData = dataManager.get(key);
			if (hedisData.isExpire()) {
				dataManager.remove(key);
				log.info("del key [{}]", key);
			}
		});
	}
}
