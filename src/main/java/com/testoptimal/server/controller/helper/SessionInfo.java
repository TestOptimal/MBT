package com.testoptimal.server.controller.helper;

public class SessionInfo {
	public String mbtSessionID;
	public String modelName;
	public boolean execCompleted;
	
	public SessionInfo (String modelName_p, String mbtSessionID_p, boolean execCompleted_p) {
		this.modelName = modelName_p;
		this.mbtSessionID = mbtSessionID_p;
		this.execCompleted = execCompleted_p;
	}
}
