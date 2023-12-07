package com.testoptimal.server.controller.helper;

import java.lang.reflect.Constructor;
import java.util.List;

import com.testoptimal.exec.ModelRunner;

public abstract class SessionMgr {
	
	private static SessionMgr sessMgr;
	
	// adds MbtStarter to the session
	public abstract void addMbtStarter (ModelRunner mbtStarter_p);
	
	// opens MbtStarter for a model, returns existing MbtStarter if model is already open
	public abstract ModelRunner getMbtStarterForModel (String modelName_p, String sessId_p);
	
	// returns a list of model MbtStarter currently opened by the session id
	public abstract List<ModelRunner> getMbtStarterForUserSession(String sessId_p);
	
	// returns the model MbtStarter for mbt session id
	public abstract ModelRunner getMbtStarterForMbtSession(String mbtSessId_p);

	// closes model, returns model closed. null if model not found.
	public abstract void closeModel (String modelName_p, String sessId_p);
	
	// close model session, return true if the model session was found and removed
	public abstract void closeModel (ModelRunner modelExec_p);


	// closes all models opened by the session id
	public abstract void closeModelAll (String sessId_p);
	
	
	public static void instantiate (String classPath_p) throws Exception {
		if (sessMgr != null) {
			throw new Exception ("SessionMgr already instantiated");
		}
    	Class aClass = Class.forName(classPath_p);
    	Constructor constructor = aClass.getConstructor();
    	sessMgr = (SessionMgr) constructor.newInstance();
	}
	
	public static SessionMgr getInstance () {
		return sessMgr;
	}
}
