package com.testoptimal.exec.sequencer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.graph.Edge;

public class TransGuard {
	private static Logger logger = LoggerFactory.getLogger(TransGuard.class);

	private MbtScriptExecutor scriptExec;
	private ExecutionSetting execSetting;
	private StateNetwork networkObj;
	
	private int consecutiveGuardResolves = 0;
	private Transition guardedTrans = null;
	private int MaxGuardResolves = 5;

	public TransGuard(MbtScriptExecutor scriptExec_p, ExecutionSetting execSetting_p) {
		this.scriptExec = scriptExec_p;
		this.execSetting = execSetting_p;
		this.networkObj = execSetting_p.getNetworkObj();
		Integer g = (Integer) execSetting_p.getOption("Optimal.MaxGuardResolves");
		if (g!=null) {
			this.MaxGuardResolves = g;
		}

	}
	
	public void reset() {
		this.guardedTrans = null;
		this.consecutiveGuardResolves = 0;
	}
	
	public Transition checkTrans(Transition trans_p, SequencePath seqPath_p ) throws MBTAbort {
		if (this.scriptExec.evalGuard(trans_p.getTransNode())) {
			if (trans_p==this.guardedTrans) {
				logger.info("Guard resolved on transition " + this.guardedTrans.getTransNode().getEvent() + " at attempt " + this.consecutiveGuardResolves + "");
				this.consecutiveGuardResolves = 0;
				this.guardedTrans = null;
			}
			return trans_p;
		}
		
		this.consecutiveGuardResolves++;
		if (this.guardedTrans==null) {
			this.guardedTrans = trans_p;
		}
		if (this.consecutiveGuardResolves > this.MaxGuardResolves) {
			throw new MBTAbort ("Unable to resolve guard on transition " + guardedTrans.getTransNode().getEvent() + ": " + this.guardedTrans.getTransNode().getGuard());
		}
		logger.info("Guard failed on transition " + this.guardedTrans.getTransNode().getEvent() + ", attempt to find alternate path (count: " + this.consecutiveGuardResolves + ")");
		Transition trans = this.findAltTrans(trans_p, seqPath_p);
		if (trans==null) {
			throw new MBTAbort ("Unable to find alternate path to resolve guard error on transition " + this.guardedTrans.getEventId() + ": " + this.guardedTrans.getTransNode().getGuard());
		}
		return trans;
	}
	

	/**
	 * 
	 * @param curState_p
	 * @param prevTravObj_p trav (trans or state) that lead to the current state.  Used to
	 * detect if it just exited from its submodel.
	 * @return
	 * @throws Exception
	 */
	private Transition findAltTrans (Transition trans_p, SequencePath seqPath_p) throws MBTAbort {
		// select one of the trans from the same
		//   state which has guard evaluates to true and insert it plus shortest path to cur trans to the transList
		// find one of trans (alt trans) from the same source state with guard evaluates to true
		State curState = (State) trans_p.getFromNode();
		Transition altTrans = this.findValidTrans(curState);
		if (altTrans==null) {
			String exceptMsg = curState.getStateId() + "." + trans_p.getTransNode().getEvent() 
					+ ": Unresolved guard: " + trans_p.getTransNode().getGuard();
			throw new MBTAbort (exceptMsg);
		}
		
		double cost = trans_p.getDist();
		trans_p.setDist(5000);
		Transition satisfyingTrans = this.findSatisfyingTransForGuard(trans_p);
		if (satisfyingTrans==null) {
			// randomly select a trans from this.transList
			satisfyingTrans = this.findLeastTraversed(this.execSetting.getAllTransitions(), this.execSetting.getRandObj(), trans_p);
		}
		
		List<Transition> tempPath1 = this.findShortestPath((State)altTrans.getToNode(), (State)satisfyingTrans.getFromNode());
		List<Transition> tempPath2 = this.findShortestPath((State)satisfyingTrans.getToNode(), (State)altTrans.getFromNode());
		trans_p.setDist(cost);
		List<Transition> transList = new java.util.ArrayList<>(tempPath1.size() + tempPath2.size() + 2);
		transList.add(0, altTrans);
		transList.addAll(tempPath1);
		transList.add(satisfyingTrans);
		transList.addAll(tempPath2);
		
		altTrans = seqPath_p.addAltRoute (transList);
		return altTrans;
	}
	
	/**
	 * returns a transition that is legal under the context of state.  If
	 * more than one transitions match, it randomly select one.
	 * GIve more weight to less traversed trans.
	 * 
	 * @param fromStateObj_p
	 * @return
	 */
	private Transition findValidTrans(State fromStateObj_p) throws MBTAbort {
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
				throw new MBTAbort (t.getMessage());
			}
		}
		
		Transition retTrans = this.findLeastTraversed(validTransList, this.execSetting.getRandObj());
		return retTrans;
	}
	

	/**
	 * returns the transition that is the guard resolver for the transition.  If
	 * multiple transitions are found, it randomly select one from the matched transitions.
	 */
	protected Transition findSatisfyingTransForGuard(Transition transObj_p) {
		List<Transition> setTransList = this.findGuardResolverTrans(transObj_p);
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
		List<Transition> retList = this.networkObj.findShortestPath(fromState_p.getId(), toState_p.getId());
		for (Transition trans: retList) {
			trans.setDist(trans.getDist()+1);
		}
		return retList;
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
	

	/**
	 * returns the list transitions that is the guard resolver for the trans passed in.
	 * @return
	 */
	private List<Transition> findGuardResolverTrans (Transition transObj_p) {
		List<Transition> activeTransList = this.networkObj.getActiveTransList();
		String r = transObj_p.getTransNode().getGuardResolvers();
		if (r==null || r.equals("")) {
			return activeTransList;
		}
		
		activeTransList = Arrays.asList(r.split(",")).stream().map(u -> (Transition) this.networkObj.findByUID(u))
			.filter(t -> t.getMinTraverseCount() > 0)
			.collect(Collectors.toList());
		return activeTransList;
	}

	
}
