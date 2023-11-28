package com.testoptimal.exec;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.scxml.StopCondition;
import com.testoptimal.util.misc.JVMStatus;

public class ExecutionStatus {

	public StopCondition stopCond;
	
	/**
	 * memory used in kb.
	 */
	public long memoryUsed;
	
	/**
	 * memory max (-Xmx setting)
	 */	
	public long memoryMax;
	public long memoryFree;

	public long memoryUsedPct;
	public String memoryMaxS;

	public String mbtMode;
	
	public java.util.Date startDT;
	public java.util.Date endDT;
	public String execStatus;
	
	/**
	 * returns the progress completion percentage of current mbt execution including all threads.
	 */
	public int progressPcnt;
	
	/**
	 * returns the remain time in millis
	 */
	@JsonIgnore
	public long remainingMillis;
	public String remainingTime;
	
	/**
	 * elapse time in millis
	 */
	@JsonIgnore
	public long elapseMillis;
	public String elapseTime;
	
	
	public ExecutionStatus (ExecutionDirector execDir_p) {
		JVMStatus jvmStatus = JVMStatus.getJVMStatus();
		
		this.memoryMax = jvmStatus.getMemMax();
		this.memoryFree = jvmStatus.getMemFree();
		this.memoryUsed = jvmStatus.getMemUsed();
		this.memoryUsedPct = ((int)this.memoryUsed*100/this.memoryMax);
		this.memoryMaxS = JVMStatus.memoryToString(this.memoryMax);

		ExecutionSetting execSetting = execDir_p.getExecSetting();

		
		this.mbtMode = execSetting.getCurMbtMode();
		this.startDT = execDir_p.getStartTime();
		this.endDT = execDir_p.getEndTime();
		this.execStatus = execDir_p.isAborted()? "ABORTED": this.endDT==null? "RUNNING":"COMPLETED";
		
		this.stopCond = execDir_p.getExecSetting().getStopCondition();
		this.progressPcnt = execDir_p.getStopMonitor().getProgressPercent();
		
		this.elapseMillis = execDir_p.getElapseMillis();
		this.elapseTime = this.millisToString(this.elapseMillis);
		if (this.progressPcnt > 100) {
			this.progressPcnt = 100;
		}
		if (this.progressPcnt > 0) {
			this.remainingMillis = (int) (this.elapseMillis * 100 / this.progressPcnt - this.elapseMillis);
			this.remainingTime = this.millisToString(this.remainingMillis);
		}
	}
	
	private String millisToString (long millis_p) {
		long seconds = millis_p/1000;
		long minutes = seconds/60;
		seconds = seconds % 60;
		long hours = minutes / 60;
		minutes = minutes % 60;
		long days = hours / 24;
		hours = hours % 24;
		
		StringBuffer retBuf = new StringBuffer();
		if (days>0) retBuf.append(days).append("d ");
		if (hours<10) retBuf.append("0");
		retBuf.append(hours).append(":");
		if (minutes<10) retBuf.append("0");
		retBuf.append(minutes).append(":");
		if (seconds<10) retBuf.append("0");
		retBuf.append(seconds);
		return retBuf.toString();
	}
}
