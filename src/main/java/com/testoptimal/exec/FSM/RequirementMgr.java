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
