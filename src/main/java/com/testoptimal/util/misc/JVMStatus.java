package com.testoptimal.util.misc;

public class JVMStatus {
	private static Runtime runtime;
	static {
		runtime = Runtime.getRuntime();
	}
	
	/**
	 * memory used in kb.
	 */
	private long memUsed;

	/**
	 * memory max (-Xmx setting)
	 */
	private long memMax;
	
	/**
	 * memory free: max - used
	 */
	private long memFree;

	
	public long getMemUsed() { return this.memUsed; }
	public long getMemMax() { return this.memMax; }
	public long getMemFree() { return this.memFree; }
	
	public String getMemUsedName() { return memoryToString(this.memUsed); }
	public String getMemMaxName() { return memoryToString(this.memMax); }
	public String getMemFreeName() { return memoryToString(this.memFree); }
	
	public static JVMStatus getJVMStatus() {
		JVMStatus retStatus = new JVMStatus();
		retStatus.memMax = runtime.maxMemory()/1024;
		retStatus.memFree = runtime.freeMemory()/1024;
		retStatus.memUsed = runtime.totalMemory()/1024 - retStatus.memFree;
		retStatus.memFree = retStatus.memMax - retStatus.memUsed;
		return retStatus;
	}
	
	
	public int getMemUsedPct() {
		return Math.round(100 * this.memUsed / this.memMax);
	}

	public static String memoryToString(long memoryKb_p) {
		String ret = String.valueOf(memoryKb_p) + "KB";
		double tempMem = memoryKb_p/1024;
		if (tempMem<=0.9) return ret;
		ret = String.valueOf(Math.round(tempMem)) + "MB";
		tempMem = tempMem/1024;
		if (tempMem<=0.9) return ret;
		else return String.valueOf(Math.round(tempMem))+"GB";
	}

	private JVMStatus() {
		
	}
}
