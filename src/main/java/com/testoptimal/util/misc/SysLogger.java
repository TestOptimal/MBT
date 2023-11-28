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
