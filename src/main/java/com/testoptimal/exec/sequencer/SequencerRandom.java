package com.testoptimal.exec.sequencer;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionDirector;
import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.Sequencer;
import com.testoptimal.exec.navigator.TraversalCount;

import openOptima.graph.Edge;

/**
 * this class selects the sequence paths based on the traversal style:
 * random (select a transition as it goes) or path based (pre-generated paths).
 * 
 * @author yxl01
 *
 */
public class SequencerRandom implements Sequencer {
	private static Logger logger = LoggerFactory.getLogger(SequencerRandom.class);

	private ExecutionDirector execDir;
	
	private StateNetwork networkObj;
	private State homeState;
	private State curState;
	private Transition curTrans;
	private Random randObj;
    private TraversalCount travTransCount;
	
	
	public SequencerRandom (ExecutionDirector execDir_p) throws MBTAbort, Exception {
		this.execDir = execDir_p;
		this.networkObj = this.execDir.getExecSetting().getNetworkObj();
		this.homeState = this.networkObj.getHomeState();
    	this.randObj = this.execDir.getExecSetting().getSeededRandObj();
    	this.curState = this.homeState;
	}
	
	
	@Override
	public Transition getNext() {
		if (this.travTransCount==null) {
			this.travTransCount = this.execDir.getSequenceNavigator().getTravTransCount();		
		}

		if (this.curTrans != null) {
			this.curState = (State) this.curTrans.getToNode();
		}
		if (this.curState.isModelFinal()) {
			this.curState = this.homeState;
		}
		List<Transition> validTransList = this.getValidTransList(this.curState, travTransCount, this.execDir);
		List<Transition> reqTransList = this.keepReqTrans(validTransList, travTransCount);
		List<Transition> leastTravList = this.removeTraversedTrans(validTransList, travTransCount);
		List<Transition> checkList = leastTravList.isEmpty()? reqTransList.isEmpty()? validTransList: reqTransList: leastTravList;
		if (checkList.isEmpty()) return null;
		
		this.curTrans = this.randomTransFromList(checkList);
//		travTransCount.addTravTrans(transObj);
		return this.curTrans;
	}
	
	/**
	 * trans that meet guards conditions and not traversed over the max number allowed.
	 * @param curState_p
	 * @param transTravCount_p
	 * @param travMgr_p
	 * @return
	 * @throws Exception
	 */
	private List<Transition> getValidTransList (State curState_p, TraversalCount transTravCount_p, ExecutionDirector execDir_p) {
		List <Transition> transList = new java.util.ArrayList <>();
		java.util.ArrayList <Edge> tempList = curState_p.getEdgesOut();
		Boolean guardStatus;
		for (Edge edge: tempList) {
			Transition transObj = (Transition) edge;
			guardStatus = execDir_p.getScriptExec().evalGuard(transObj.getTransNode());
			if (guardStatus && (transObj.isFake() || transTravCount_p.getTravCount(transObj) < transObj.getMaxTraverseCount())) {
				transList.add(transObj);
			}
		}
		return transList;
	}

	private List<Transition> keepReqTrans (List<Transition> transList_p, TraversalCount transTravCount_p) {
		return transList_p.stream().filter(transObj -> transObj.isFake() || transTravCount_p.isRequired(transObj)).collect(Collectors.toList());
	}

	private List<Transition> removeTraversedTrans (List<Transition> transList_p, TraversalCount transTravCount_p) {
		return transList_p.stream().filter(transObj -> transTravCount_p.getTravCountLeft(transObj) > 0).collect(Collectors.toList());
	}

	private Transition randomTransFromList (List<Transition> transList_p) {
		int totalWeight = transList_p.stream()
				.map( transObj -> (transObj.getTransNode()==null?5:transObj.getTransNode().getWeight()))
				.reduce(0, Integer::sum);

		// generate random number and pick the trans based on the randowm number and the trans weight
		int idx = this.randObj.nextInt(totalWeight);
		totalWeight = 0;
		for (Transition transObj: transList_p) {
			int weight = (transObj.getTransNode()==null?5:transObj.getTransNode().getWeight());
			totalWeight = totalWeight + weight;
			if (idx <= totalWeight) {
				return transObj;
			}
		}
		// fall back to first trans
		return (Transition) transList_p.get(0);
	}
	
	@Override
	public boolean isStartingPath() {
		return this.curState == this.homeState;
	}
	
	@Override
	public boolean isEndingPath() {
		return ((State) this.curTrans.getToNode()).isModelFinal();
	}
	
	@Override
	public int getPathCount() {
		return 0;
	}

}
