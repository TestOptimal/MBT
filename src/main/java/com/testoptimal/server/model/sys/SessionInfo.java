package com.testoptimal.server.model.sys;

import java.util.Date;

public class SessionInfo {
	 public String sessionID;
	 public String model;
	 public String mode;
	 public String progress;
	 public int threads;
	 public String elapseTime; 
	 public String status;
	 public Date startTime;
	 public Date endTime;

	 public SessionInfo () {
		 return;
	 }
}
