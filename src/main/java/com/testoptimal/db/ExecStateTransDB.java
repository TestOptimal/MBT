package com.testoptimal.db;

import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;

public class ExecStateTransDB {
	public String UID;
	public String stateName;
	public String transName;
	public String type;
	public int minTravRequired;
	public Integer expectedMillis;
	public int passCount;
	public int failCount;
	public Integer avgMillis;
	public Integer minMillis;
	public Integer maxMillis;
	public int slowCount;
	
	public ExecStateTransDB(StateNode state_p) {
		this.UID = state_p.getUID();
		this.stateName = state_p.getStateID();
		this.type = "state";
		this.minTravRequired = 1;
		this.expectedMillis = state_p.getMaxMillis();
		if (this.expectedMillis < 0 || this.expectedMillis >= 2147483647) {
			this.expectedMillis = null;
		}
	}

	public ExecStateTransDB(TransitionNode trans_p) {
		this.stateName = trans_p.getParentStateNode().getStateID();
		this.transName = trans_p.getEvent();
		this.type = "trans";
		this.UID = trans_p.getUID();
		this.minTravRequired = trans_p.getTraverseTimes();
		this.expectedMillis = trans_p.getMaxMillis();
		if (this.expectedMillis < 0 || this.expectedMillis >= 2147483647) {
			this.expectedMillis = null;
		}

	}
}
