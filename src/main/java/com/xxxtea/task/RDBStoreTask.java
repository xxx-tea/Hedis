package com.xxxtea.task;

import cn.hutool.core.io.FileUtil;
import com.esotericsoftware.kryo.KryoException;
import com.xxxtea.common.Instance;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.manager.DataManager;
import com.xxxtea.model.HedisData;
import com.xxxtea.serializer.KryoSerializer;
import com.xxxtea.util.FileHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RDBStoreTask implements Runnable {
	DataManager dataManager = DataManager.INSTANCE;
	File file = new File(HedisConfig.INSTANCE.getRDB().getPath());

	@SneakyThrows
	public RDBStoreTask() {
		this.loadDataFromFile();
	}

	private void loadDataFromFile() throws IOException {
		try {
			Instance.TIMER.start();
			FileHelper.createIfNotExist(file);
			Map<String, HedisData> map = KryoSerializer.readObject(file);
			dataManager.setMap(map);
			log.info("rdb init {} key! size: [{}] cost: [{}],",
					map.size(), FileUtil.readableFileSize(file),
					Instance.TIMER.intervalPretty());
		} catch (IOException e) {
			log.warn("rdb file read failed cause 'not exist'");
		} catch (KryoException e) {
			log.warn("rdb file read failed cause 'serialize failed'");
			KryoSerializer.writeObject(file, new HashMap<>());
		}
	}

	@Override
	public void run() {
		try {
			Map<String, HedisData> map = dataManager.getMap();
			KryoSerializer.writeObject(file, map);
		} catch (IOException e) {
			FileHelper.createIfNotExist(file);
			log.error("rdb write error", e);
		}
	}
}
