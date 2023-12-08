package com.testoptimal.exec.exception;

/**
 * Signal MBT execution abort
 * @author yxl01
 *
 */
public class MBTAbort extends Throwable {
	public MBTAbort (String exceptionMsg_p) {
		super(exceptionMsg_p);
//		super(StringUtil.removeInvalidXMLChar(exceptionMsg_p));
	}
}
