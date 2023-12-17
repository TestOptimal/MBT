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
