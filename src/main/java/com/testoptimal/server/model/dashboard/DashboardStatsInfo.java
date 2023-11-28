package com.testoptimal.server.model.dashboard;

import java.util.List;

public class DashboardStatsInfo {
	public List<String> projList;
	public List<String> kpiSetList;
	public String[] collectIntervals = new String[] {"Daily", "Weekly", "BiWeekly", "Monthly"};
}
