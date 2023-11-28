package com.testoptimal.server.model.dashboard;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.testoptimal.server.config.Config;
import com.testoptimal.util.FileUtil;

public class DashboardConfig {

	public Page page;
	public List<GraphGroup> groups;
	
	public static DashboardConfig getConfig() throws Exception {
		Gson gson = new Gson();
		DashboardConfig config = gson.fromJson(FileUtil.readFile(Config.getDashRoot() + "DashboardConfig.json").toString(), DashboardConfig.class);
//		config.groups = config.graphs.stream().filter(x-> x!=null).collect(Collectors.toList());
		return config;
	}
	
	public void writeConfig () throws Exception {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileUtil.writeToFile(Config.getDashRoot() + "DashboardConfig.json", gson.toJson(this));		
	}
	
}
