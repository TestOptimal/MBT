package com.testoptimal.server.model;

import java.util.List;

public class AlmSource {
	public List<SourceInfo> sources = new java.util.ArrayList<>();
	
	public void addSource(String name_p, String desc_p) {
		SourceInfo s = new SourceInfo();
		s.name = name_p;
		s.desc = desc_p;
		this.sources.add(s);
	}
	
	public class SourceInfo {
		public String name;
		public String desc;
	}
}
