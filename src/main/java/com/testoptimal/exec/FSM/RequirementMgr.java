package com.testoptimal.exec.FSM;

import java.lang.reflect.Constructor;
import java.util.Properties;

import com.testoptimal.server.security.ManageUser;

public class RequirementMgr {
	private static ManageRequirement requirementMgr;
	
	public static void instantiate (String classPath_p) throws Exception {
		if (requirementMgr != null) {
			throw new Exception ("RequirementMgr already instantiated");
		}
    	Class aClass = Class.forName(classPath_p);
    	Constructor constructor = aClass.getConstructor();
    	requirementMgr = (ManageRequirement) constructor.newInstance();
    	requirementMgr.init();	
    }
	
	public static ManageRequirement getInstance () {
		return requirementMgr;
	}
}
