package com.testoptimal.stats.exec;

public class TestCaseStepItem {
	public String assertCode;
	public String reqTag;
	public ModelExec.Status status = ModelExec.Status.failed;
	public String checkMsg;
	
	public TestCaseStepItem(String reqTag_p, ModelExec.Status passed_p, String checkMsg_p, String assertCode_p) {
		this.reqTag = reqTag_p;
		this.checkMsg = checkMsg_p;
		this.assertCode = assertCode_p;
		this.status = passed_p;
	}
	
	
	public boolean isPassed () {
		return this.status == ModelExec.Status.passed;
	}
	
}
