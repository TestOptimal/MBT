package com.testoptimal.exec.sequencer.to;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exception.MBTAbort;
import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.exec.navigator.Sequencer;
import com.testoptimal.mscript.MbtScriptExecutor;

import openOptima.graph.Edge;

/**
 * this class selects the sequence paths based on the traversal style:
 * random (select a transition as it goes) or path based (pre-generated paths).
 * 
 * @author yxl01
 *
 */
public class SequencerOptimal2 implements Sequencer {
	private static Logger logger = LoggerFactory.getLogger(SequencerOptimal2.class);
	private static int ConsecutiveAltPathLimit = 4;

	private PathOptimal pathor;
	private MbtScriptExecutor scriptExec;
	private ExecutionDirector execDir;
	private ExecutionSetting execSetting;
	
	private List<SequencePath> pathList;
	private int curPathIdx = -1;
	private SequencePath curPath = null;
	private int curTransIdxInPath = -1;
	private int consecutiveAltPathCount = 0;
	private Transition altPathTrans = null;
	
	public SequencerOptimal2 (ExecutionDirector execDir_p) throws MBTAbort, Exception {
		this.execDir = execDir_p;
		this.scriptExec = this.execDir.getScriptExec();
		this.execSetting = this.execDir.getExecSetting();
		this.pathor = new PathOptimal (this.execDir);

		this.pathList = this.pathor.genPathList();
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

		if (this.scriptExec.evalGuard(trans.getTransNode())) {
			if (trans==this.altPathTrans) {
				logger.info("Guard resolved on transition " + this.altPathTrans.getTransNode().getEvent() + " at attempt " + this.consecutiveAltPathCount + "");
				this.consecutiveAltPathCount = 0;
				this.altPathTrans = null;
			}
			return trans;
		}

		// find alternate route/trans
		if (this.altPathTrans==null) {
			this.consecutiveAltPathCount++;
			this.altPathTrans = trans;
		}
		else if (trans==this.altPathTrans) {
			this.consecutiveAltPathCount++;
		}
		
		if (this.consecutiveAltPathCount > ConsecutiveAltPathLimit) {
			logger.warn ("Unable to resolve guard on transition " + altPathTrans.getTransNode().getEvent() + ": " + this.altPathTrans.getTransNode().getGuard());
		}
		
		logger.info("Guard failed on transition " + this.altPathTrans.getTransNode().getEvent() + ", attempt to find alternate path (count: " + this.consecutiveAltPathCount + ")");
		trans = this.findDetour(trans);
		if (trans==null) {
			logger.warn ("Unable to find alternate path to resolve guard error on transition " + this.altPathTrans.getEventId() + ": " + this.altPathTrans.getTransNode().getGuard());
			return null;
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

	/**
	 * 
	 * @param trans_p current transition
	 * @return alt trans for detour
	 * @throws Exception
	 */
	private Transition findDetour (Transition trans_p) {
		// select one of the trans from the same
		//   state which has guard evaluates to true and insert it plus shortest path to cur trans to the transList
		// find one of trans (alt trans) from the same source state with guard evaluates to true
		State curState = (State) trans_p.getFromNode();
		Transition altTrans = this.findValidTrans(curState);
		if (altTrans==null) {
			String exceptMsg = curState.getStateId() + "." + trans_p.getTransNode().getEvent() 
					+ ": Unresolved guard: " + trans_p.getTransNode().getGuard();
			logger.warn (exceptMsg);
			return null;
		}
		
		double cost = trans_p.getDist();
		trans_p.setDist(5000);
		Transition satisfyingTrans = this.findSatisfyingTransForGuard(trans_p);
		if (satisfyingTrans==null) {
			// randomly select a trans from this.transList
			satisfyingTrans = this.findLeastTraversed(execSetting.getAllTransitions(), this.execSetting.getRandObj(), trans_p);
		}
		
		try {
			List<Transition> tempPath1 = this.findShortestPath((State)altTrans.getToNode(), (State)satisfyingTrans.getFromNode());
			List<Transition> tempPath2 = this.findShortestPath((State)satisfyingTrans.getToNode(), (State)altTrans.getFromNode());
			trans_p.setDist(cost);
			List<Transition> transList = new java.util.ArrayList<>(tempPath1.size() + tempPath2.size() + 2);
			transList.add(0, altTrans);
			transList.addAll(tempPath1);
			transList.add(satisfyingTrans);
			transList.addAll(tempPath2);
			altTrans = this.curPath.addAltRoute (this.curTransIdxInPath, transList);
			return altTrans;
		}
		catch (Throwable e) {
			logger.warn(e.getMessage());
			return null;
		}
	}
	
	/**
	 * returns a transition that is legal under the context of state.  If
	 * more than one transitions match, it randomly select one.
	 * GIve more weight to less traversed trans.
	 * 
	 * @param fromStateObj_p
	 * @return
	 */
	private Transition findValidTrans(State fromStateObj_p) {
		List<Transition> validTransList = new java.util.ArrayList<Transition>();
		List<Edge> outgoingTransList = fromStateObj_p.getEdgesOut();
		for (int i=0; i<outgoingTransList.size(); i++) {
			Transition transObj = (Transition) outgoingTransList.get(i);
			try {
				if (this.scriptExec.evalGuard(transObj.getTransNode())) {
					validTransList.add(transObj);
				}
			}
			catch (Throwable t) {
				logger.warn (t.getMessage());
				return null;
			}
		}
		
		Transition retTrans = this.findLeastTraversed(validTransList, this.execSetting.getRandObj());
		return retTrans;
	}
	
	/**
	 * returns the transition that sets the state var to the specified value.  If
	 * multiple transitions are found, it randomly select one from the matched transitions.
	 */
	protected Transition findSatisfyingTransForGuard(Transition transObj_p) {
		List<Transition> setTransList = this.execSetting.getNetworkObj().getSetTransForGuard(transObj_p, this.scriptExec);
		if (setTransList.isEmpty()) return null;
		
		Transition retTrans = this.findLeastTraversed(setTransList, this.execSetting.getRandObj());
		return retTrans;
	}
	
	/**
	 * returns the shortest path of transitions from fromState_p to toState_p.
	 * @param fromState_p
	 * @param toState_p
	 * @return
	 */
	public List<Transition> findShortestPath (State fromState_p, State toState_p) throws MBTAbort {
		StateNetwork networkObj = this.pathor.getNetworkObj();
		synchronized (networkObj) {
			List<Transition> retList = networkObj.findShortestPath(fromState_p.getId(), toState_p.getId());
			for (Transition trans: retList) {
				trans.setDist(trans.getDist()+1);
			}
			return retList;
		}
	}
	
	public Transition findLeastTraversed(List<Transition> transList_p, Random randObj_p) {
		return findLeastTraversed(transList_p, randObj_p, null);
	}
	
	public Transition findLeastTraversed(List<Transition> transList_p, Random randObj_p, Transition exceptTrans_p) {
		if (transList_p.isEmpty()) return null;
		if (transList_p.size()==1) return transList_p.get(0);
		List<Transition> transList = new java.util.ArrayList<Transition> (transList_p.size());
		List<Integer> validTransWeightList = new java.util.ArrayList<Integer>();
		int maxWeight = 0;
		for (int i=0; i<transList_p.size(); i++) {
			Transition transObj = transList_p.get(i);
			if (transObj!=exceptTrans_p) {
				transList.add(transObj);
				int travWeight = transObj.getTraversedCount() - transObj.getMinTraverseCount();
				validTransWeightList.add(travWeight);
				if (maxWeight<travWeight) {
					maxWeight = travWeight;
				}
			}
		}
		
		// adjust weight: max - transWeight so that less traversed trans gets more weight
		int totalWeight = 0;
		for (int i=0; i<validTransWeightList.size(); i++) {
			int transWeight = maxWeight - validTransWeightList.get(i);
			if (transWeight<=0) transWeight = 1;
			
			validTransWeightList.set(i, transWeight);
			totalWeight += transWeight;
		}
		int idx = randObj_p.nextInt(totalWeight);
		totalWeight = 0;
		for (int i=0; i<validTransWeightList.size(); i++) {
			totalWeight += validTransWeightList.get(i);
			if (totalWeight > idx) {
				return transList.get(i);
			}
		}
		return transList.get(transList.size()-1);
	}
	

}
