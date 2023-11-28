package com.testoptimal.db;

public class ExecReqDB {
	public String reqTag;
	public String priority;
	public int passCount = 0;
	public int failCount = 0;
	
	public ExecReqDB(String reqTag_p, String priority_p) {
		this.reqTag = reqTag_p;
		this.priority = priority_p;
	}
}
