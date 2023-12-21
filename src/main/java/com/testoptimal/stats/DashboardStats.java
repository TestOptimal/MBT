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

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.testoptimal.server.config.Config;
import com.testoptimal.stats.exec.ModelExec;
import com.testoptimal.stats.exec.ModelExec.Status;
import com.testoptimal.util.FileUtil;

public class DashboardStats {
	public Date collectDate = new Date();
	public Date minDate = new Date();
	public Date maxDate = new Date (0);
	
	public StatsSummary overallStats = new StatsSummary();
	public StatsSummary lastWeekStats = new StatsSummary();
	public StatsSummary last24HrStats = new StatsSummary();
	public StatsSummary latestRunStats = new StatsSummary();
	
	public static DashboardStats getStats() {
		DashboardStats dstats = new DashboardStats();
		Gson gson = new Gson();
		
		String folder = Config.getRootPath() + "dashboard/";
		File df = new File(folder);
		Map<String, List<ModelExecSummary>> mlist = Arrays.asList(df.list()).stream()
			.filter(f -> f.endsWith(".json"))
			.map(f -> {
				try {
					ModelExecSummary sum = (ModelExecSummary) gson.fromJson(FileUtil.readFile(folder + f).toString(), ModelExecSummary.class);
					dstats.minDate = dstats.minDate.after(sum.startDT)?sum.startDT:dstats.minDate;
					dstats.maxDate = dstats.maxDate.before(sum.endDT)?sum.endDT:dstats.maxDate;
					return sum;
				}
				catch (Exception e) {
					return null;
				}
			})
			.filter(s -> s!=null)
			.collect(Collectors.groupingBy(m -> m.modelName));
		mlist.values().stream().forEach(m -> dstats.addModelExec(m));

		return dstats;
	}
	
	public void addModelExec(List<ModelExecSummary> modelExecSumList_p) {
		Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
		Date yesterdayDate = cal.getTime();
        cal.add(Calendar.DATE, -6);
		Date lastWeekStartDate = cal.getTime();
		
		modelExecSumList_p.sort((ModelExecSummary a, ModelExecSummary b) -> b.startDT.compareTo(a.startDT));
		this.overallStats.modelNum++;
		modelExecSumList_p.stream().forEach(e -> this.overallStats.addModelSummary(e));
		ModelExecSummary latestExecSum = modelExecSumList_p.get(0);
		if (latestExecSum.status==Status.failed) {
			this.overallStats.modelFailed++;
			if (lastWeekStartDate.before(latestExecSum.startDT)) {
				this.lastWeekStats.modelFailed++;
			}
			if (yesterdayDate.before(latestExecSum.startDT)) {
				this.last24HrStats.modelFailed++;
			}
		}
		if (modelExecSumList_p.stream().filter(e -> e.startDT.after(lastWeekStartDate))
			.map(e -> this.lastWeekStats.addModelSummary(e)).count() >= 0) {
			this.lastWeekStats.modelNum++;
		}
		if (modelExecSumList_p.stream().filter(e -> e.startDT.after(yesterdayDate))
			.map(e -> this.last24HrStats.addModelSummary(e)).count() >= 0) {
			this.last24HrStats.modelNum++;
		}
		this.latestRunStats.modelNum++;
		this.latestRunStats.addModelSummary(modelExecSumList_p.get(0));
	}

	
	public static void addModelExec (ModelExec modelExec_p) throws Exception {
		String dashboardFolder = Config.getRootPath() + "dashboard/";
		File f = new File(dashboardFolder);
		if (!f.exists()) {
			f.mkdir();
		}
		Gson gson = new Gson();
		FileUtil.writeToFile(dashboardFolder + modelExec_p.mbtSessID + ".json", gson.toJson(modelExec_p.execSummary));
	}

	public static void purge (int retentionDays_p) throws Exception {
		String dashboardFolder = Config.getRootPath() + "dashboard/";
		File f = new File(dashboardFolder);
		if (!f.exists()) {
			f.mkdir();
		}
		
		long retentionMillis = System.currentTimeMillis() - retentionDays_p * 86400000; 
		Arrays.asList(f.list()).stream().filter(fname -> fname.endsWith(".json"))
			.map(fname -> new File (dashboardFolder, fname))
			.filter(f2 -> f2.lastModified() < retentionMillis)
			.forEach(f2 -> f2.delete());
	}

	
	public class StatsSummary {
		public int modelNum;
		public int modelFailed;
		
		public int modelExecNum;
		public int modelExecFailed;
		public int testCaseNum;
		public int testCaseFailed;
		public int checkNum;
		public int checkFailed;
		public int reqNum;
		public int reqFailed;
		public int reqNotCovered;
		

		public ModelExecSummary addModelSummary (ModelExecSummary modelSum_p) {
			this.modelExecNum++;
			if (modelSum_p.status==Status.failed) this.modelExecFailed++;
			this.testCaseNum += modelSum_p.testCaseNum;
			this.testCaseFailed += modelSum_p.testCaseFailed;
			this.checkNum += modelSum_p.testItemNum;
			this.checkFailed += modelSum_p.testItemNum;
			this.reqNum += modelSum_p.reqNum;
			this.reqFailed += modelSum_p.reqFailed;
			this.reqNotCovered += modelSum_p.reqNotCovered;
			return modelSum_p;
		}
	}
}
