package com.testoptimal.exec.sequencer.to;

import java.util.List;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.NoSolutionException;
import openOptima.NotImplementedException;

public class MutantSequencer extends SequencerPathBase {
	private MutantPath mutantPath;
    public MutantSequencer (ExecutionDirector execDir_p) throws Exception {
    	super(execDir_p);
    	this.mutantPath = new MutantPath(this.getShortestPathObj());
    }

	@Override
	public List<SequencePath> genSeqPaths() throws NoSolutionException,
			InterruptedException, NotImplementedException {
    	List<Transition> reqTransList = this.getNetworkObj().getAllRequiredTrans();
    	reqTransList.sort(new PriorityTrans());
		
		List<SequencePath> pathList = this.mutantPath.genSeqPaths(this.getNetworkObj().getHomeState(), reqTransList);
		return pathList;
	}
}
