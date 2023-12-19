/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

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

