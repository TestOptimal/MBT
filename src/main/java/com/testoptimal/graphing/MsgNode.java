package com.testoptimal.graphing;

import com.testoptimal.util.StringUtil;


public class MsgNode {

	public static final String ROOT = "_ROOT";
	
	private String name;
	private String uid;
	private String stereotype = null;
	private int sortNum = 0;

	public MsgNode(String nodeName_p, String uid_p, String stereotype_p) {
		this.name = nodeName_p;
		this.uid = uid_p;
		this.stereotype = stereotype_p;
	}
	
	public void setSortNum (int sortID_p) {
		this.sortNum = sortID_p;
	}
	int getSortNum () {
		return this.sortNum;
	}
	
	public String getNodeName() { 
		return this.name;
	}
	
	public String getUID() {
		return this.uid;
	}
	
	public boolean isSame(String uid_p) {
		if (this.uid.equalsIgnoreCase(uid_p)) {
			return true;
		}
		else return false;
	}
	
	// stereotype can only contain one char
	public String getStereotype() {
		if (this.stereotype!=null && this.stereotype.length()>1) {
			return this.stereotype.toLowerCase().substring(0,1);
		}
		else return this.stereotype;
	}
	
	public boolean hasStereotype(String stereotype_p) {
		if (this.stereotype==null) return false;
		return (this.stereotype.trim().equalsIgnoreCase(stereotype_p));
	}
	
	public void setStereotype(String stereotype_p) {
		if (StringUtil.isEmpty(this.stereotype)) {
			this.stereotype = stereotype_p;
		}
	}
}
