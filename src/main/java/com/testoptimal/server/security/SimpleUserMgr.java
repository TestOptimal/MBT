package com.testoptimal.server.security;

import com.testoptimal.server.config.Config;

public class SimpleUserMgr implements ManageUser {
	private String password = "";
	private String username = "";
	
	public void init () {
		this.username = Config.getProperty("License.Email"); 
		this.password = Config.getProperty("security.password." + this.username); 
	}
	
	@Override
	public boolean authenticate (String userid_p, String userPwd_p) {
		if (userid_p == null || userPwd_p == null || this.username == null || this.password == null) {
			return false;
		}
		return userid_p.equalsIgnoreCase(this.username) && userPwd_p.equals(this.password);
	}
	
	@Override
	public void registerUser (String username_p, String password_p) throws Exception {
		this.username = username_p;
		this.password = password_p;
		Config.setProperty("License.Email", username_p);
		Config.setProperty("security.password." + username_p, password_p);
	}

	@Override
	public boolean isAdmin(String username_p) {
		return this.username.equalsIgnoreCase(username_p);
	}
}
