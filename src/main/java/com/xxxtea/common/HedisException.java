package com.xxxtea.common;

public class HedisException extends RuntimeException {
	public HedisException(String message) {
		super(message);
	}

	public HedisException(Throwable cause) {
		super(cause);
	}
}
