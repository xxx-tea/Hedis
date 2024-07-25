package com.xxxtea.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
	/**
	 * 是否记录到AOF日志中
	 */
	boolean aof() default false;
}
