package com.xxxtea.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.xxxtea.config.HedisConfig;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.util.FileHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class AOFStoreTask implements Runnable {
	File file = new File(HedisConfig.INSTANCE.getAOF().getPath());

	public AOFStoreTask() {
		FileHelper.createIfNotExist(file);
	}

	@Override
	public void run() {
		List<String> commandList = CommandManager.INSTANCE.pollAll();
		// 没有命令不进行文件流开启关闭操作
		if (CollUtil.isNotEmpty(commandList)) {
			FileUtil.appendLines(commandList, file, StandardCharsets.UTF_8);
		}
	}
}
