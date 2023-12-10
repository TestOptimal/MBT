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
	private MbtScriptExecutor scriptExec;
	
	public SequencerRandom (ExecutionDirector execDir_p) throws MBTAbort, Exception {
		this.execDir = execDir_p;
		this.scriptExec = execDir_p.getScriptExec();
		this.networkObj = this.execDir.getExecSetting().getNetworkObj();
		this.networkObj = new StateNetwork ();
		this.networkObj.init(this.execDir.getExecSetting().getModelMgr().getScxmlNode());
		this.homeState = this.networkObj.getHomeState();
    	this.randObj = this.execDir.getExecSetting().getSeededRandObj();
    	this.curState = this.homeState;
	}
	
	
	@Override
	public Transition getNext() {
		if (this.travTransCount==null) {
			this.travTransCount = this.execDir.getSequenceNavigator().getTravTransCount();		
		}
		if (this.curTrans!=null) {
			this.curState = (State) this.curTrans.getToNode();
		}
		if (this.curState.isModelFinal()) {
			this.curState = this.homeState;
		}
		else if (this.curState.isFinal()) {
			List<Transition> outTransList = this.curState.getEdgesOut().stream().map(e -> (Transition) e).toList();
			Transition trans = outTransList.get(this.randObj.nextInt(outTransList.size()));
			this.curTrans = trans.getForTrans();
			this.curState = (State) this.curTrans.getFromNode();
			return this.curTrans;
		}
		else if (this.curState.isSuperVertex()) {
			this.curState = (State) this.curState.findSubModelEntryTrans().getToNode();
		}
		
		this.curTrans = this.findNext();
		return this.curTrans;
	}
	
	private Transition findNext() {
		List <Transition> validTransList = this.curState.getEdgesOut().stream()
			.map(edge -> (Transition) edge)
			.filter(transObj -> this.scriptExec.evalGuard(transObj.getTransNode()))
			.filter(transObj -> transObj.isFake() || travTransCount.getTravCount(transObj) < transObj.getMaxTraverseCount())
			.toList();

		List<Transition> reqTransList = validTransList.stream().filter(transObj -> transObj.isFake() || travTransCount.isRequired(transObj)).toList();
		List<Transition> leastTravList = validTransList.stream().filter(transObj -> travTransCount.getTravCountLeft(transObj) > 0).toList();
		List<Transition> checkList = leastTravList.isEmpty()? reqTransList.isEmpty()? validTransList: reqTransList: leastTravList;
		if (checkList.isEmpty()) return null;

		int totalWeight = checkList.stream()
				.map( transObj -> (transObj.getTransNode()==null?5:transObj.getTransNode().getWeight()))
				.reduce(0, Integer::sum);

		// generate random number and pick the trans based on the randowm number and the trans weight
		int idx = this.randObj.nextInt(totalWeight);
		totalWeight = 0;
		for (Transition transObj: checkList) {
			int weight = (transObj.getTransNode()==null?5:transObj.getTransNode().getWeight());
			totalWeight = totalWeight + weight;
			if (idx <= totalWeight) {
				return transObj;
			}
		}
		// fall back to first trans
		return (Transition) checkList.get(0);
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
