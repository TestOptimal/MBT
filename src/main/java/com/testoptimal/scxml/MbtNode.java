package com.testoptimal.scxml;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.util.StringUtil;

public final class MbtNode {
	private String uid = ""; // unique id
	public String getUID() { return this.uid;}

	private String mode="Optimal";
	public String getMode() {
		return this.mode;
	}
	
	private String initScript;
	public String getInitScript() {
		return this.initScript;
	}

	private StopCondition stopCond = new StopCondition();
	public StopCondition getStopCond() {
		return this.stopCond;
	}

//	private int execThreadNum = 1; // # of threads. Will be used for parallel processing in Enterprise Edition
//	public void setExecThreadNum(int execThreadNum_p) { this.execThreadNum = execThreadNum_p; }
//	public int getExecThreadNum() {
//		return this.execThreadNum;
//	}

//	private int threadSpreadDelay = 0; // # of ms to delay between thread startups
//	public int getThreadSpreadDelay() { return this.threadSpreadDelay; }
//	public void setThreadSpreadDelay(int delayMs) { this.threadSpreadDelay = delayMs; }
//	
	// key=val pair, separated by comma or semi-colon
	private String seqParams = ""; // parameters for sequencer
	public void setSeqParams(String params_p) {
		if (params_p==null) this.seqParams = "";
		else this.seqParams = params_p;
	}
	public String getSeqParams() {
		if (this.seqParams==null) {
			this.seqParams = "";
		}
		return this.seqParams;
	}

	@JsonIgnore
	public static boolean isSpecified(int in_p) { 
		if (in_p<=0 || in_p>=Integer.MAX_VALUE) return false;
		else return true;
	}

//	/**
//	 * returns the mbtMode string
//	 * @param nodeList_p
//	 * @return
//	 */
//	@JsonIgnore
//	public String getMbtModeString() {
//		if (this.mode==null) this.mode = "";
//		return this.mode;
//	}
//	
//	/**
//	 * resolves a string to an MbtMode enum.
//	 */
//	public static String resolveMbtMode(String mbtMode_p) {
//		try {
//			return MbtMode.valueOf(mbtMode_p);
//		}
//		catch (Exception e) {
//			return null;
//		}
//	}
//	
//	public void setMbtMode(String mbtModeString_p) {
//		this.mode = mbtModeString_p;
//	}
	
	public void setOptions (Map<String, Object> options_p) {
		for (java.util.Map.Entry<String, Object> entry : options_p.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value==null || StringUtil.isEmpty(value.toString())) continue;
    		if (key.equalsIgnoreCase("seqParams")) {
    			this.setSeqParams((String) value);
    		}
//    		else if (key.equalsIgnoreCase("threadNum")) {
//    			this.setExecThreadNum((int) value);
//    		}
//    		else if (key.equalsIgnoreCase("threadDelay")) {
//    			this.setThreadSpreadDelay((int) value);
//    		}
		}
		
		this.stopCond.setOptions(options_p);
	}
}
