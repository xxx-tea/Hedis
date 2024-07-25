package com.xxxtea.util;

import cn.hutool.core.util.StrUtil;
import com.xxxtea.common.HedisException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Assert {
	String errorMsgTemplate = "Wrong number of arguments for '{}' command";

	public void checkParamsCount(boolean expression) {
		if (!expression) {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			StackTraceElement caller = stackTrace[2];
			throw new HedisException(StrUtil.format(errorMsgTemplate, caller.getMethodName()));
		}
	}
}
