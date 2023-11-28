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
