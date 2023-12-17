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

import java.util.Map;

import com.testoptimal.server.config.Config;

public class TOUserMgr implements ManageUser {
	/**
	 * internal use within TO server. Not for use for License key
	 */
	private static Cypher cyp = new Cypher("d4d9 b70a 86d8 6a86 270b e0d7 e00c 11ea");
	public static String webDecrypt (String inString_p) throws Exception { return cyp.decrypt(inString_p);}
	public static String webEncrypt (String inString_p) throws Exception { return cyp.encrypt(inString_p);}

	private static Map<String, String> userPwdList = new java.util.HashMap<>();
	private static String adminUserName;
	
	public void init () {
		adminUserName = Config.getProperty("License.Email", "");
		
	    // check password encoding
		Config.getPropertiesByPrefix("security.password.").entrySet().stream().forEach(e -> {
			String key = (String)e.getKey();
			String pwd = (String)e.getValue();
			if (!pwd.startsWith("enc(") || !pwd.endsWith(")")) {
				try {
					Config.setProperty(key, "enc(" + TOUserMgr.webEncrypt(pwd) + ")");
				}
				catch (Exception ex) {
					// ok, just use as is
				}
			}
			
			String userid = ((String)e.getKey()).substring("security.password.".length());
			pwd = (String)e.getValue();
			try {
				if (pwd.startsWith("enc(") && pwd.endsWith(")")) {
    				pwd = TOUserMgr.webDecrypt(pwd.substring(4, pwd.length()-1));
    			}
			}
			catch (Exception ex) {
				// ok, just use as is
			}
    		userPwdList.put(userid.toLowerCase(), pwd);
		});
	}
	
		
	@Override
	public boolean authenticate (String userid_p, String userPwd_p) {
		if (userid_p == null || userPwd_p == null) {
			return false;
		}
		String expPwd = userPwdList.get(userid_p.toLowerCase());
		return expPwd !=null && expPwd.equals(userPwd_p);
	}
	
	@Override
	public void registerUser (String username_p, String password_p) throws Exception {
		userPwdList.put(username_p, password_p);
		Config.setProperty("security.password." + username_p, "enc(" + TOUserMgr.webEncrypt(password_p) + ")");
		if (adminUserName == null) {
			adminUserName = username_p;
			Config.setProperty("License.Email", adminUserName);
		}
	}
	
	@Override
	public boolean isAdmin(String username_p) {
		return adminUserName!=null && adminUserName.equalsIgnoreCase(username_p);
	}
}
