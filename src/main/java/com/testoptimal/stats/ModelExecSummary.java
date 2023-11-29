package com.testoptimal.stats;

import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.db.ModelExecDB;
import com.testoptimal.db.ModelExecDB.Status;

public class ModelExecSummary {
	public String modelName;
	public String mbtSessID;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public java.util.Date startDT;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public java.util.Date endDT;	
	
	public String mbtSequencer;
	public ModelExecDB.Status status;

	public int stateNum = 0;
	public int stateCovered = 0;
	public int stateTraversal = 0;
	public int transNum = 0;
	public int transCovered = 0;
	public int transTraversal = 0;

	public int reqNum = 0;
	public int reqPassed = 0;
	public int reqFailed = 0;
	public int reqCovered = 0;
	public int reqNotCovered = 0;
	public int reqTraversal = 0;

	public int testCaseNum;
	public int testCasePassed;
	public int testCaseFailed;
	public int testStepNum;
	public int testItemNum;
	
	
	@JsonIgnore
	public void summarize(ModelExecDB modelExec_p) {
		this.stateCovered = (int) modelExec_p.stateTransList.stream().filter(s -> s.transName == null)
				.filter(s -> s.passCount + s.failCount >= s.minTravRequired).count();
		this.transCovered = (int) modelExec_p.stateTransList.stream().filter(s -> s.transName != null)
				.filter(s -> s.passCount + s.failCount >= s.minTravRequired).count();
		this.stateTraversal = modelExec_p.stateTransList.stream().filter(s -> s.transName == null).collect(Collectors.summingInt(s -> s.passCount+s.failCount));
		this.transTraversal = modelExec_p.stateTransList.stream().filter(s -> s.transName != null).collect(Collectors.summingInt(s -> s.passCount+s.failCount));
		
		this.reqNum = modelExec_p.reqList.size();
		this.reqPassed = (int) modelExec_p.reqList.stream().filter(r -> r.passCount > 0 && r.failCount == 0).count();
		this.reqFailed = (int) modelExec_p.reqList.stream().filter(r -> r.failCount > 0).count();
		this.reqNotCovered = modelExec_p.reqList.size() - this.reqPassed - this.reqFailed;
		this.reqCovered = this.reqNum - this.reqNotCovered;
		this.reqTraversal = (int) modelExec_p.reqList.stream().collect(Collectors.summingInt(s -> s.passCount + s.failCount));
		
		this.testCaseNum = modelExec_p.tcList.size();
		this.testCasePassed = (int) modelExec_p.tcList.stream().filter(t -> t.status==Status.passed).count();
		this.testCaseFailed = (int) modelExec_p.tcList.stream().filter(t -> t.status==Status.failed).count();
		this.testStepNum = (int) modelExec_p.tcList.stream().flatMap(t -> t.stepList.stream()).count();
		this.testItemNum = (int) modelExec_p.tcList.stream().flatMap(t -> t.stepList.stream()).flatMap(s -> s.itemList.stream()).count();
	}
}
	