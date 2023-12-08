package com.testoptimal.server.model.agent;


public class TestResult {
	public boolean passed;
	public String result;
	public String reqTag;
	public String assertID;
	
	public TestResult(boolean passed_p, String result_p) {
		this.passed = passed_p;
		this.result = result_p;
	}
}