package com.testoptimal.server.controller.helper;

import java.util.ArrayList;
import java.util.List;

import com.testoptimal.exec.ModelRunner;

public class SimpleSessionMgr extends SessionMgr {
	private ModelRunner MbtSession;
	
	@Override
	public void addMbtStarter(ModelRunner mbtStarter_p) {
		if (this.MbtSession!=null) {
			this.MbtSession.stopMbt();
		}
		this.MbtSession = mbtStarter_p;
	}

	@Override
	public List<ModelRunner> getMbtStarterForUserSession(String sessId_p) {
		List<ModelRunner> list = new ArrayList<ModelRunner>();
		if (this.MbtSession!=null) {
			list.add(this.MbtSession);
		}
		return list;
	}

	@Override
	public ModelRunner getMbtStarterForModel(String modelName_p, String sessId_p) {
		if (this.MbtSession!=null && this.MbtSession.getModelMgr().getModelName().equals(modelName_p)) {
			return this.MbtSession;
		}
		else return null;
	}

	@Override
	public ModelRunner getMbtStarterForMbtSession(String modelName_p, String mbtSessId_p) {
		if (this.MbtSession!=null && this.MbtSession.getMbtSessionID().equals(mbtSessId_p)) {
			return this.MbtSession;
		}
		else return null;
	}

	@Override
	public void closeModel(String modelName_p, String sessId_p) {
		this.MbtSession = null;
	}

	@Override
	public void closeModel(ModelRunner modelExec_p) {
		this.MbtSession = null;
	}

	@Override
	public void closeModelAll(String sessId_p) {
		this.MbtSession = null;
	}

}