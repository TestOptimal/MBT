package com.testoptimal.scxml;

import java.util.Map;



public class BoxNode {
	private transient boolean readOnly=false;

	private String uid; // unique id
	private String typeCode = "box";
	private String name = "";
	private String color;
	private String textColor;
	private String notepad;
	
	private Map<String,Integer> position = new java.util.HashMap<String,Integer>();

	public void setName (String name_p) {
		if (name_p==null) this.name="";
		else this.name = name_p;
	}
	
	public String getName() {
		return this.name==null?"":this.name;
	}
}


