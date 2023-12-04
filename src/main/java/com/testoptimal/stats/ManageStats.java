package com.testoptimal.stats;

import java.util.List;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.stats.exec.ModelExec;

public interface ManageStats {
	public List<ModelExec> getStatsList(ModelMgr modelMgr_p) throws Exception;
	public ModelExec getStats (ModelMgr modelMgr_p, String mbtSessID_p) throws Exception;
	public void save(ModelExec execStats_p) throws Exception;
	
	// returns # of deleted
	public int deleteStats (String modelName_p, List<String> mbtSessIDList_p);
}
