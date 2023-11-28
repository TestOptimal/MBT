package com.testoptimal.exec.sequencer.to;

import java.util.List;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;

/**
 * Generate optimal test sequence to cover all trans or marked transitions.
 * 
 * @author yxl01
 *
 */
public class OptimalSequencer extends SequencerPathBase {
	private OptimalPath optimalPath;

	public OptimalSequencer (ExecutionDirector execDir_p) throws Exception {
    	super(execDir_p);
    	
    	ExecutionSetting execSetting = execDir_p.getExecSetting();
    	ModelMgr modelMgr = execSetting.getModelMgr();
        StateNetwork networkObj = execSetting.getNetworkObj();
        
        List<Transition> reqTransList = networkObj.getTransByUIDList(execSetting.getMarkList());
		if (!reqTransList.isEmpty()) {
	    	networkObj.markRequiredTrans(reqTransList, this.getTraversedTransCost(), modelMgr);
		}
	    this.optimalPath = new OptimalPath(networkObj);
	}
	
	@Override
	protected List<SequencePath> genSeqPaths () throws Exception {
		List<Transition> transList = this.optimalPath.genPathList();
		List<SequencePath> retList = SequencePath.breakupToPaths(transList);
		return retList;
	}
}
