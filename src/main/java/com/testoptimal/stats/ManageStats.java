package com.testoptimal.stats;

import java.util.List;

import com.testoptimal.db.ModelExecDB;
import com.testoptimal.exec.FSM.ModelMgr;

public interface ManageStats {
	public List<ModelExecDB> getStatsList(ModelMgr modelMgr_p) throws Exception;
	public ModelExecDB getStats (ModelMgr modelMgr_p, String mbtSessID_p) throws Exception;
	public void save(ModelExecDB execStats_p) throws Exception;
	// returns # of deleted
	public int deleteStats (String modelName_p, List<String> mbtSessIDList_p);
}
