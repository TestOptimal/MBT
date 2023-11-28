package com.testoptimal.server.model.dashboard;

import java.util.List;
import java.util.Map;

public class GraphGroup {
	public Header header;
	public Map<String,Object> style; 
	public List<Map<String, Object>> graphs;
	public boolean expanded = true;
}