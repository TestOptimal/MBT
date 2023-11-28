package com.testoptimal.exec.FSM;

import java.util.List;

import com.testoptimal.server.model.Requirement;

public interface ManageRequirement {
	public void init () throws Exception;

	public List<Requirement> getRequirement(ModelMgr modelMgr_p) throws Exception;
	public void saveRequirement(ModelMgr modelMgr_p, List<Requirement> reqList_p) throws Exception;
	
}
