package com.xxxtea.common;

import com.xxxtea.parser.AbstractParser;
import com.xxxtea.parser.StringParser;
import com.xxxtea.parser.SystemParser;
import com.xxxtea.parser.ZSetParser;
import com.xxxtea.util.LogTimeInterval;

import java.util.Arrays;
import java.util.List;

public interface Instance {
	LogTimeInterval TIMER = new LogTimeInterval();
	List<AbstractParser> PARSERLIST = Arrays.asList(
			new SystemParser(),
			new StringParser(),
			new ZSetParser()
	);
}
