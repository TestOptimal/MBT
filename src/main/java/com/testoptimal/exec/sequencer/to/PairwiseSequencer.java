package com.testoptimal.exec.sequencer.to;

import java.util.List;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;

public class PairwiseSequencer extends SequencerPathBase {
	private OptimalPath optimalPath;
	private StateNetwork originalNetworkObj;
	private StateNetwork optimizeNetworkObj;

	public PairwiseSequencer (ExecutionDirector execDir_p) throws Exception {
    	super(execDir_p);
    	this.originalNetworkObj = this.getNetworkObj();
		this.optimizeNetworkObj = StateNetwork.createCoverageNetwork(this.originalNetworkObj);

		// cleanup
    	this.optimalPath = new OptimalPath(this.optimizeNetworkObj);
	}
	
	@Override
	protected List<SequencePath> genSeqPaths () throws Exception {
		List<Transition> transList = this.optimalPath.genPathList();
		List<Transition> transList2 = new java.util.ArrayList<>(transList.size()/2);
		for (Transition trans: transList) {
			if (trans.isLoopbackTrans()) {
				transList2.add(trans);
			}
			else if (trans.getTransNode()!=null) {
				transList2.add(this.originalNetworkObj.findTransByUID(trans.getTransNode().getUID()));
			}
			else if (trans.getForTrans()!=null) {
				transList2.add(this.originalNetworkObj.findTransByUID(trans.getForTrans().getTransNode().getUID()));
			}
		}
		List<SequencePath> retList = SequencePath.breakupToPaths(transList2);
		return retList;
	}
}
