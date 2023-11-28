package com.testoptimal.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncShutdown extends Thread {
	private static Logger logger = LoggerFactory.getLogger(AsyncShutdown.class);
	
	private long delayMillis = 0;
	public AsyncShutdown(long delayMs) {
		this.delayMillis = delayMs;
		if (this.delayMillis<50) this.delayMillis = 50;
	}
	
	@Override
	public void run() {
		logger.info("Shutdown cleanup");
		
		System.exit(1);
	}
}

