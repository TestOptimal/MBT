package com.testoptimal.graphing;

import com.testoptimal.util.StringUtil;

public class MsgSeq {
	private String fromUID;
	private String toUID;
	private String msg;
	private int priority;
	private String color;
	private String stereotype;
	private String tcName;
	
	public MsgSeq (String fromUID_p, String toUID_p, String msg_p, String stereotype_p, String color_p, int priority_p, String tcName_p) {
		this.fromUID = fromUID_p;
		this.toUID = toUID_p;
		this.msg = msg_p;
		this.priority = priority_p;
		this.color = color_p;
		this.stereotype = stereotype_p;
		if (this.msg==null) {
			this.msg = "";
		}
		this.tcName = tcName_p==null?"":tcName_p;
	}
	
	public String getMsg() {
		return this.msg;
	}
	
	public int getPriority() {
		return this.priority;
	}
		
	public String getFromUID() {
		return this.fromUID;
	}
	
	public String getToUID() {
		return this.toUID;
	}
	
	public String getTcName() {
		return this.tcName;
	}
	
	public String getLabel() {
		String ret = this.msg;
		if (!StringUtil.isEmpty(this.color)) {
			ret = "<color:" + this.color + ">" + this.msg + "</color>";
		}
		if (!StringUtil.isEmpty(this.stereotype)) {
			ret += " <back:cadetblue><b><i><size:11><color:#FFFFFF> " + this.stereotype  + " </color></size></i></b></back>";
		}
		return ret;
	}
	
	public String getColor() {
		if (this.color==null) return "";
		this.color = this.color.trim();
		if (this.color.startsWith("#")) return this.color;
		else return "#" + this.color;
	}
}
