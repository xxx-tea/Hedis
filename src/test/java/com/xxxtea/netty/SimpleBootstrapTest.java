package com.xxxtea.netty;

import junit.framework.TestCase;

public class SimpleBootstrapTest extends TestCase {

	public void testName() {
		SimpleBootstrap bootstrap = new SimpleBootstrap("localhost", 6379);
		bootstrap.connect();
	}
}