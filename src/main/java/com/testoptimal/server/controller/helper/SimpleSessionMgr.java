package com.testoptimal.server.controller.helper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.testoptimal.exec.ModelRunner;

public class SimpleSessionMgr extends SessionMgr {
	private List<ModelRunner> MbtSessionList = new java.util.ArrayList<>();
	
	@Override
	public ModelRunner closeModel(String modelName_p, String sessId_p) {
		ModelRunner m = this.getMbtStarterForModel(modelName_p, sessId_p);
		if (m != null) {
			this.MbtSessionList.remove(m);
		}
		return m;
	}

	@Override
	public List<ModelRunner> getMbtStarterForUserSession(String sessId_p) {
		return this.MbtSessionList.stream().filter(s -> s.getHttpSessionID().equals(sessId_p)).collect(Collectors.toList());
	}

	@Override
	public void closeModelAll(String email_p) {
		this.getMbtStarterForUserSession(email_p).stream().forEach(s -> {
			s.stopMbt();
			this.MbtSessionList.remove(s);
		});
	}

	@Override
	public ModelRunner getMbtStarterForModel(String modelName_p, String sessId_p) {
		Optional<ModelRunner> opt = this.MbtSessionList.stream().filter(s -> s.getHttpSessionID().equals(sessId_p) && s.getModelMgr().getModelName().equals(modelName_p)).findFirst();
		return opt.orElse(null);
	}

	@Override
	public void addMbtStarter(ModelRunner mbtStarter_p) {
		this.closeModel(mbtStarter_p.getModelMgr().getModelName(), mbtStarter_p.getHttpSessionID());
		this.MbtSessionList.add(mbtStarter_p);
	}

	@Override
	public ModelRunner getMbtStarterForMbtSession(String mbtSessId_p) {
		Optional<ModelRunner> opt = this.MbtSessionList.stream().filter(s -> s.getMbtSessionID().equals(mbtSessId_p)).findFirst();
		return opt.orElse(null);
	}
}
