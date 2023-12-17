/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

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
