package com.xxxtea.base;

import com.xxxtea.config.HedisConfig;
import com.xxxtea.manager.DataManager;
import com.xxxtea.task.RDBStoreTask;
import junit.framework.TestCase;
import lombok.Getter;

@Getter
public class BaseTestCase extends TestCase {
	DataManager dataManager = DataManager.INSTANCE;
	HedisConfig hedisConfig = HedisConfig.INSTANCE;
	RDBStoreTask rdbStoreTask = new RDBStoreTask();
}
