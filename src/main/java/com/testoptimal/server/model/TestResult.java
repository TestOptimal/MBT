package com.testoptimal.server.model;


public class TestResult {
	public boolean passed;
	public String result;
	
	public TestResult(boolean passed_p, String result_p) {
		this.passed = passed_p;
		this.result = result_p;
	}
}