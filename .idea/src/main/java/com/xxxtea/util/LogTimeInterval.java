package com.xxxtea.util;

import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogTimeInterval {
	TimeInterval timeInterval = new TimeInterval();

	public void log(Runnable runnable, String message) {
		String id = IdUtil.fastSimpleUUID();
		timeInterval.start(id);
		runnable.run();
		log.info("{} cost: [{}]", message, timeInterval.intervalPretty(id));
	}

	public void start() {
		timeInterval.start(getCallerId());
	}

	public String intervalPretty() {
		return timeInterval.intervalPretty(getCallerId());
	}

	private String getCallerId() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		StackTraceElement caller = stackTrace[2]; // 获取调用者信息
		return caller.getClassName() + "." + caller.getMethodName();
	}

}
