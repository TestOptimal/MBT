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

package com.testoptimal.server.controller.helper;

import java.lang.reflect.Constructor;
import java.util.List;

import com.testoptimal.exec.ModelRunner;
import com.testoptimal.server.config.Config;

public abstract class SessionMgr {
	
	private static SessionMgr sessMgr;
	
	// adds MbtStarter to the session
	public abstract void addMbtStarter (ModelRunner mbtStarter_p);
	
	// opens MbtStarter for a model, returns existing MbtStarter if model is already open
	public abstract ModelRunner getMbtStarterForModel (String modelName_p, String sessId_p);
	
	// returns a list of model MbtStarter currently opened by the session id
	public abstract List<ModelRunner> getMbtStarterForUserSession(String sessId_p);
	
	// returns the model MbtStarter for mbt session id
	public abstract ModelRunner getMbtStarterForMbtSession(String modelName_p, String mbtSessId_p);

	// closes model, returns model closed. null if model not found.
	public abstract void closeModel (String modelName_p, String sessId_p);
	
	// close model session, return true if the model session was found and removed
	public abstract void closeModel (ModelRunner modelExec_p);


	// closes all models opened by the session id
	public abstract void closeModelAll (String sessId_p);
	
	
	public static void instantiate (String classPath_p) throws Exception {
		if (sessMgr == null) {
			try {
		    	Class aClass = Class.forName(classPath_p);
		    	Constructor constructor = aClass.getConstructor();
		    	sessMgr = (SessionMgr) constructor.newInstance();				
			}
			catch (Exception e) {
				Config.postStartupError("Error loading " + classPath_p, e);
				sessMgr = new SimpleSessionMgr();
			}
		}
	}
	
	public static SessionMgr getInstance () {
		return sessMgr;
	}
}
