package com.testoptimal.server.model;

import com.testoptimal.exec.ModelRunner;
import com.testoptimal.server.controller.helper.SessionMgr;

// IDE model state
public class ModelState {
	public String modelName;
	public boolean running = false;
	public String mbtSessID;
	
	public ModelState (String modelName_p, String sessId_p) {
		this.modelName = modelName_p;
		ModelRunner sess = SessionMgr.getInstance().getMbtStarterForModel(modelName_p, sessId_p);
		if (sess!=null) {
			this.running =  sess.isRunning();
			this.mbtSessID = sess.getMbtSessionID();
		}
	}
}
