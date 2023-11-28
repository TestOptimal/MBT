package com.testoptimal.server.model.parser;

import java.util.List;

public class GherkinScenario {
	public String scenarioName;
	public String givenState;
	public List<String> whenScriptList = new java.util.ArrayList<>();
	public String thenState;
	
	public String getGivenState () {
		return GherkinModel.getCode(this.givenState);
	}

	public String getThenState () {
		return GherkinModel.getCode(this.thenState);
	}
}
