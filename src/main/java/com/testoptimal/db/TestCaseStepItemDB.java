package com.testoptimal.db;

public class TestCaseStepItemDB {
	public String assertCode;
	public String reqTag;
	public ModelExecDB.Status status = ModelExecDB.Status.failed;
	public String checkMsg;
	
	public TestCaseStepItemDB(String reqTag_p, ModelExecDB.Status passed_p, String checkMsg_p, String assertCode_p) {
		this.reqTag = reqTag_p;
		this.checkMsg = checkMsg_p;
		this.assertCode = assertCode_p;
		this.status = passed_p;
	}
	
	
	public boolean isPassed () {
		return this.status == ModelExecDB.Status.passed;
	}
	
}
