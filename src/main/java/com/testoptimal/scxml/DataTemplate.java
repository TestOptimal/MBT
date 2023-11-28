package com.testoptimal.scxml;

import com.google.gson.Gson;

public class DataTemplate {
	public StateNode state = new StateNode();
	public TransitionNode transition = new TransitionNode();
	public SwimlaneNode swimlane = new SwimlaneNode();
	public BoxNode box = new BoxNode();
	
	public static String getTemplateJson () {
		DataTemplate t = new DataTemplate();
		Gson gson = new Gson();
		String json = gson.toJson(t);
		return json;
	}
}
