package com.testoptimal.exec.sequencer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.exec.navigator.Sequencer;

import openOptima.NoSolutionException;
import openOptima.graph.Vertex;
import openOptima.network.postman.LinZhaoAlgorithm;
import openOptima.network.postman.PostmanPath;

/**
 * this class selects the sequence paths based on the traversal style:
 * random (select a transition as it goes) or path based (pre-generated paths).
 * 
 * @author yxl01
 *
 */
public class SequencerOptimal implements Sequencer {
	private static Logger logger = LoggerFactory.getLogger(SequencerOptimal.class);

	private ExecutionDirector execDir;
	private ExecutionSetting execSetting;
	
	private List<SequencePath> pathList;
	private int curPathIdx = -1;
	private SequencePath curPath = null;
	private int curTransIdxInPath = -1;
	private int traversedTransCost = 500;
	
	public SequencerOptimal (ExecutionDirector execDir_p) throws MBTAbort, Exception {
		this.execDir = execDir_p;
		this.execSetting = this.execDir.getExecSetting();

    	ModelMgr modelMgr = execSetting.getModelMgr();
        StateNetwork networkObj = execSetting.getNetworkObj();
        List<Transition> reqTransList = networkObj.getTransByUIDList(execSetting.getMarkList());
		if (!reqTransList.isEmpty()) {
	    	networkObj.markRequiredTrans(reqTransList, this.traversedTransCost, modelMgr);
		}

		this.pathList = this.genPathList();
		logger.info("paths to cover: " + this.pathList.size());
		if (!this.pathList.isEmpty()) {
			this.curPathIdx = 0;
			this.curTransIdxInPath = -1;
			this.curPath = this.pathList.get(0); 
		}
	}
	
	@Override
	public Transition getNext() {
		if (this.curPath == null) return null;
		
		
		this.curTransIdxInPath++;
		Transition trans = this.curPath.getTransAt(this.curTransIdxInPath);
		boolean newPath = (trans == null);
		if (newPath) {
			this.curPathIdx++;
			if (this.curPathIdx >= this.pathList.size()) return null;
			this.curPath = this.pathList.get(this.curPathIdx);
			this.curTransIdxInPath = 0;
			trans = this.curPath.getTransAt(this.curTransIdxInPath);
		}

		return trans;
	}
	
	@Override
	public boolean isStartingPath() {
		return this.curTransIdxInPath == 0;
	}
	
	@Override
	public boolean isEndingPath() {
		return this.curTransIdxInPath == this.curPath.getTransList().size() - 1;
	}

	@Override
	public int getPathCount() {
		return this.pathList.size();
	}
	
	public List<SequencePath> genPathList () throws Exception {
		StateNetwork networkObj = this.execSetting.getNetworkObj();
		int homeStateID = networkObj.getHomeState().getId(); 
		LinZhaoAlgorithm optimizer;
    	optimizer = new LinZhaoAlgorithm();
		optimizer.init(networkObj);
		
		try {
			PostmanPath pathObj = optimizer.getPostmanPath(homeStateID);
			List<Transition> transList = StateNetwork.cleanPath(pathObj.getPathArcs());
			List<SequencePath> retList = SequencePath.breakupToPaths(transList);
			return retList;
		}
		catch (NoSolutionException e) {
			StringBuffer exceptBuf = new StringBuffer();
			java.util.ArrayList<String> procdList = new java.util.ArrayList<String>();
			for (Vertex vertexObj: optimizer.getUnReachableVertexList()) {
				String transHashCode = vertexObj.getMarker();
				procdList.add(transHashCode);
			}
			
			// there is a loop that involves the required transition. Euler path found but not covering initial/end state.
			if (procdList.isEmpty()) {
				exceptBuf.append("Unable to find path. Try to add/mark additional transition may help, e.g. transition from initial state.");
			}
			else {
				exceptBuf.append("Unable to reach following states from initial state: ");
				for (int i=1; i<procdList.size(); i++) {
					if (i>1) exceptBuf.append(",");
					exceptBuf.append(procdList.get(i));
				}
			}
			throw new NoSolutionException(exceptBuf.toString());
		}
	}
}
