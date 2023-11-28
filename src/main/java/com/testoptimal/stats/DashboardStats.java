package com.testoptimal.stats;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.testoptimal.db.ModelExecDB;
import com.testoptimal.db.ModelExecDB.Status;

public class DashboardStats {
	public Date collectDate = new Date();
	public StatsSummary overallStats = new StatsSummary();
	public StatsSummary lastWeekStats = new StatsSummary();
	public StatsSummary last24HrStats = new StatsSummary();
	public StatsSummary latestRunStats = new StatsSummary();
	
	public void addModelExec(List<ModelExecDB> modelExecList_p) {
		if (modelExecList_p.isEmpty()) return;
		Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
		Date yesterdayDate = cal.getTime();
        cal.add(Calendar.DATE, -6);
		Date lastWeekStartDate = cal.getTime();
		
		this.overallStats.modelNum++;
		modelExecList_p.stream().forEach(e -> this.overallStats.addModelSummary(e.execSummary));
		ModelExecSummary latestExecSum = modelExecList_p.get(0).execSummary;
		if (latestExecSum.status==Status.failed) {
			this.overallStats.modelFailed++;
			if (lastWeekStartDate.before(latestExecSum.startDT)) {
				this.lastWeekStats.modelFailed++;
			}
			if (yesterdayDate.before(latestExecSum.startDT)) {
				this.last24HrStats.modelFailed++;
			}
		}
		if (modelExecList_p.stream().filter(e -> e.execSummary.startDT.after(lastWeekStartDate))
			.map(e -> this.lastWeekStats.addModelSummary(e.execSummary)).count() >= 0) {
			this.lastWeekStats.modelNum++;
		}
		if (modelExecList_p.stream().filter(e -> e.execSummary.startDT.after(yesterdayDate))
			.map(e -> this.last24HrStats.addModelSummary(e.execSummary)).count() >= 0) {
			this.last24HrStats.modelNum++;
		}
		this.latestRunStats.modelNum++;
		this.latestRunStats.addModelSummary(modelExecList_p.get(0).execSummary);
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
		
		// returns model exec status
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
