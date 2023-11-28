package com.testoptimal.server.security;

public interface ManageUser {
	
	public void init() throws Exception;
	public boolean authenticate (String username_p, String password_p);
	public void registerUser (String username_p, String password_p) throws Exception;
	public boolean isAdmin(String username_p);
}
