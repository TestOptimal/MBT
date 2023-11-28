package com.testoptimal.server.model;

public class Requirement implements Comparable <Requirement> {
//	public static enum Priority {high, medium, low};
	public String name;
	public String desc;
	public String priority;

	@Override
	public int compareTo(Requirement req_p) {
		return this.name.compareTo(req_p.name);
	}
	
	public Requirement(String name_p, String desc_p, String priority_p) {
		this.name = name_p;
		this.desc = desc_p;
		this.priority = priority_p;
	}
}
