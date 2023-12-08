package com.testoptimal.exec.exception;

import java.util.List;

import com.testoptimal.util.StringUtil;

/**
 * MBT execution exception resulted from validation error.  This exception does not stop the MBT Execution.  However this
 * exception is recorded in the execution stats.
 * @author yxl01
 *
 */
public class MBTException extends Exception {

	private static final long serialVersionUID = 1L;
	private String mbtMessage;
	private boolean printed = false;

	public MBTException (String exceptionMsg_p) {
		super(StringUtil.removeInvalidXMLChar(exceptionMsg_p));
	}
	
	public boolean isPrinted() {
		return this.printed;
	}
	
	public void setPrinted() {
		this.printed = true;
	}
	
	
	public static MBTException mergeList(List<MBTException> exceptList_p) {
		StringBuffer msg = new StringBuffer();
		int i = 0;
		for (MBTException except: exceptList_p) {
			if (i>0) msg.append(", ");
			msg.append(except.getMessage());
		}
		
		return new MBTException (msg.toString());
	}
	
	@Override
	public String getMessage() {
		if (StringUtil.isEmpty(this.mbtMessage)) {
			return super.getMessage();
		}
		else {
			return this.mbtMessage;
		}
	}
	
	public void setMessage (String mbtMessage_p) {
		this.mbtMessage = mbtMessage_p;
	}
	
	public void appendToMessage (String appendMsg_p) {
		this.mbtMessage = this.getMessage() + appendMsg_p;
	}
}
