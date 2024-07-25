package com.xxxtea.util;

import com.xxxtea.common.HedisException;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class TimeHelper {
	public TimeUnit parse(String unit) {
		switch (unit) {
			case "S":
			case "SECONDS":
				return TimeUnit.SECONDS;
			case "M":
			case "MINUTES":
				return TimeUnit.MINUTES;
			case "H":
			case "HOURS":
				return TimeUnit.HOURS;
			case "D":
			case "DAYS":
				return TimeUnit.DAYS;
			default:
				throw new HedisException("TimeUnit should in [SECONDS,MINUTES,HOURS,DAYS]");
		}
	}
}
