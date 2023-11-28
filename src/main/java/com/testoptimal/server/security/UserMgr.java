package com.testoptimal.server.security;

import java.lang.reflect.Constructor;
import java.util.Properties;

public class UserMgr {
	private static ManageUser userMgr;
	
	public static void instantiate (String classPath_p) throws Exception {
		if (userMgr != null) {
			throw new Exception ("UserMgr already instantiated");
		}
    	Class aClass = Class.forName(classPath_p);
    	Constructor constructor = aClass.getConstructor();
    	userMgr = (ManageUser) constructor.newInstance();
    	userMgr.init();	
    }
	
	public static ManageUser getInstance () {
		return userMgr;
	}
}
