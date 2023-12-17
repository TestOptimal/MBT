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
