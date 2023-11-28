package com.testoptimal.server.model;

/**
 * a user may be shared by multiple http sessions (browsers)
 * @author yxl01
 *
 */
public class UserSession {

	public String userid;
	public UserSession (String userid_p) {
		this.userid = userid_p;
	}
	

}
