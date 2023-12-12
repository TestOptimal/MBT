package com.testoptimal.server.controller.helper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.testoptimal.exec.ModelRunner;

public class MultiSessionMgr extends SessionMgr {
	private List<ModelRunner> MbtSessionList = new java.util.ArrayList<>();
	
	@Override
	public void addMbtStarter(ModelRunner mbtStarter_p) {
		this.closeModel(mbtStarter_p.getModelMgr().getModelName(), mbtStarter_p.getHttpSessionID());
		this.MbtSessionList.add(mbtStarter_p);
//		this.MbtSessionList = this.MbtSessionList.stream().filter(m -> m.isRunning()).toList();
	}

	@Override
	public List<ModelRunner> getMbtStarterForUserSession(String sessId_p) {
		return this.MbtSessionList.stream().filter(s -> s.getHttpSessionID().equals(sessId_p)).collect(Collectors.toList());
	}

	@Override
	public ModelRunner getMbtStarterForModel(String modelName_p, String sessId_p) {
		Optional<ModelRunner> opt = this.MbtSessionList.stream().filter(s -> s.getHttpSessionID().equals(sessId_p) && s.getModelMgr().getModelName().equals(modelName_p)).findFirst();
		return opt.orElse(null);
	}

	@Override
	public ModelRunner getMbtStarterForMbtSession(String modelName_p, String mbtSessId_p) {
		Optional<ModelRunner> opt = this.MbtSessionList.stream().filter(s -> s.getModelMgr().getModelName().equals(modelName_p) && s.getMbtSessionID().equals(mbtSessId_p)).findFirst();
		return opt.orElse(null);
	}

	@Override
	public void closeModel(String modelName_p, String sessId_p) {
		ModelRunner m = this.getMbtStarterForModel(modelName_p, sessId_p);
		if (m != null) {
			this.MbtSessionList.remove(m);
		}
	}

	@Override
	public void closeModel(ModelRunner modelExec_p) {
		this.MbtSessionList.remove(modelExec_p);
	}

	@Override
	public void closeModelAll(String email_p) {
		this.getMbtStarterForUserSession(email_p).stream().forEach(s -> {
			s.stopMbt();
			this.MbtSessionList.remove(s);
		});
	}
}
