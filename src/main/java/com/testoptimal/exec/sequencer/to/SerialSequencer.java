package com.testoptimal.exec.sequencer.to;

import java.util.List;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.network.shortestpath.ShortestPathProblem;

/**
 * This sequencer is similar to mCaseSequencer except that it generates the test sequence
 * by finding the shortest path to connect all transitions/states in the mCase.
 * 
 * @author yxl01
 *
 */
public class SerialSequencer extends SequencerPathBase {
	public SerialSequencer (ExecutionDirector execDir_p) throws Exception {
    	super(execDir_p);
    	return;
	}

	@Override
	public List<SequencePath> genSeqPaths() throws Exception {
		StateNetwork network = this.getNetworkObj();
		int homeStateID = network.getHomeState().getId(); 
		int fromStateID = homeStateID;
		int toStateID;
		ShortestPathProblem sp = this.getShortestPathObj();
		List<SequencePath> retList = new java.util.ArrayList<>();
		List<String> markUIDList = this.getExecSetting().getMarkList();
		if (markUIDList==null) markUIDList = new java.util.ArrayList<String>();
		List<Transition> transList = new java.util.ArrayList<>();
		for (String uid: markUIDList) {
			Object uidObj = network.findByUID(uid);
			if (uidObj==null) continue;
			if (uidObj instanceof State) {
				toStateID = ((State) uidObj).getId();
			    transList.addAll(SequencerPathBase.getShortestPathTransList(sp, fromStateID, toStateID));
				fromStateID = toStateID;
			}
			else {
				Transition transObj = (Transition) uidObj;
				toStateID = transObj.getFromNode().getId();
			    transList.addAll(SequencerPathBase.getShortestPathTransList(sp, fromStateID, toStateID));
			    transList.add(transObj);
				fromStateID = transObj.getToNode().getId();
			}
		}
		if (this.getExecSetting().isStopAtFinalOnly()) {
		    transList.addAll(SequencerPathBase.getShortestPathTransList(sp, fromStateID, homeStateID));
		}
		SequencePath path = new SequencePath(transList);
		retList.add(path);
		return retList;
	}
}

