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

package com.testoptimal.util.misc;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SysLogger {
	private static Logger logObj = LoggerFactory.getLogger(SysLogger.class);

	public static void logInfo (String msg_p) {
		SysLogger.logObj.info(msg_p);
	}
	
	public static void logDebug (String msg_p) {
		SysLogger.logObj.debug(msg_p);
	}

	public static void logError (String msg_p, Throwable ex_p) {
		SysLogger.logObj.error(msg_p, ex_p);
	}
	
	public static void logError (String msg_p) {
		SysLogger.logObj.error(msg_p);
	}
	
	public static void logWarn (String msg_p) {
		SysLogger.logObj.warn(msg_p);
	}
}
