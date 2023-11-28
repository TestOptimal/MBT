package com.testoptimal.stats;

import java.util.concurrent.atomic.AtomicInteger;

import com.testoptimal.server.config.Config;

public class SysStats {
	private static AtomicInteger stateModelRuns = new AtomicInteger(0);
	private static AtomicInteger combModelRuns = new AtomicInteger(0);
	
	static {
		String st = Config.getProperty("stats.runs", "0,0");
		try {
			String[] list = st.split(",");
			stateModelRuns.set(Integer.parseInt(list[0]));
			combModelRuns.set(Integer.parseInt(list[1]));
		}
		catch (Exception e) {
			// ok, just use 0
		}
	}
	
	public static String getString () {
		return "S" + stateModelRuns + "C" + combModelRuns;
	}

	public static void addStateRun() {
		stateModelRuns.incrementAndGet();
		save();
	}

	public static void addCombRun() {
		combModelRuns.incrementAndGet();
		save();
	}
	
	private static void save() {
		try {
			Config.setProperty("stats.runs", stateModelRuns.get()+","+combModelRuns.get());
			Config.save();
		}
		catch (Exception e) {
			// ok
		}
	}
}
