package com.xxxtea.util;

import cn.hutool.core.io.FileUtil;
import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileHelper {
	public void createIfNotExist(File file) {
		if (!FileUtil.exist(file)) {
			FileUtil.touch(file);
		}
	}
}
