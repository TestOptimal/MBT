package com.testoptimal.exec.sequencer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTAbort;
import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.exec.navigator.SequencePath;
import com.testoptimal.scxml.TransitionNode;

public class TransGuard {
	private static Logger logger = LoggerFactory.getLogger(TransGuard.class);

	private MbtScriptExecutor scriptExec;
	private StateNetwork networkObj;
	
	private int consecutiveGuardResolves = 0;
	private Transition guardedTrans = null;
	private int MaxGuardResolves = 5;
	private Random rand = new Random();

	public TransGuard(MbtScriptExecutor scriptExec_p, ExecutionSetting execSetting_p) {
		this.scriptExec = scriptExec_p;
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
	
	// returns the next trans to traverse
	public Transition checkTrans(Transition trans_p, SequencePath seqPath_p ) throws MBTAbort {
		if (this.scriptExec.evalGuard(trans_p.getTransNode())) {
			if (trans_p==this.guardedTrans) {
				logger.info("Guard resolved on transition " + this.guardedTrans.getTransNode().getEvent() + " at attempt " + this.consecutiveGuardResolves + "");
				this.consecutiveGuardResolves = 0;
				this.guardedTrans = null;
			}
			return trans_p;
		}
		
		// guard failed, find alternate trans to traverse and adjust the path
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
	
	private Transition findAltTrans (Transition trans_p, SequencePath seqPath_p) throws MBTAbort {
		State curState = (State) trans_p.getFromNode();
		TransitionNode transNode = trans_p.getTransNode();
		List<Transition> list1 = curState.getEdgesOut().stream()
				.map(t -> (Transition) t)
				.filter(t -> this.scriptExec.evalGuard(((Transition) t).getTransNode()))
				.toList();
		List<Transition> list2 = list1.stream().filter(t -> transNode.isGuardResolver(t.getTransNode().getUID())).toList();
		if (list2.isEmpty()) {
			list2 = list1;
		}
		if (list2.isEmpty()) {
			String exceptMsg = curState.getStateId() + "." + trans_p.getTransNode().getEvent() 
					+ ": Unresolved guard: " + trans_p.getTransNode().getGuard();
			throw new MBTAbort (exceptMsg);
		}
		Transition altTrans = list2.get(this.rand.nextInt(list2.size()));
		
		Transition resolverTrans = null;
		if (transNode.isGuardResolver(altTrans.getTransNode().getUID())) {
			resolverTrans = altTrans;
		}
		else {
			List<Transition> tlist = Arrays.asList(trans_p.getTransNode().getGuardResolvers().split(",")).stream()
					.map(u -> (Transition) this.networkObj.findByUID(u))
					.toList();
			if (tlist.size() > 1) {
				List<Transition> list4 = tlist.stream().filter(t -> t.getMinTraverseCount() > 0).toList();
				if (list4.size() > 1) {
					tlist = list4;
				}
			}
			resolverTrans = tlist.get(this.rand.nextInt(tlist.size()));
		}
		
		double transCost = trans_p.getDist();
		trans_p.setDist(5000);
		List<Transition> altPath = this.findShortestPath((State)altTrans.getToNode(), (State)resolverTrans.getFromNode());
		if (altTrans!=resolverTrans) {
			altPath.add(resolverTrans);
			altPath.addAll(this.findShortestPath((State)resolverTrans.getToNode(), (State)altTrans.getFromNode()));
		}
		trans_p.setDist(transCost);
		altPath.add(0, altTrans);		
		altTrans = seqPath_p.addAltRoute (altPath);
		return altTrans;
	}
	
	public List<Transition> findShortestPath (State fromState_p, State toState_p) throws MBTAbort {
		List<Transition> retList = this.networkObj.findShortestPath(fromState_p.getId(), toState_p.getId());
		for (Transition trans: retList) {
			trans.setDist(trans.getDist()+1);
		}
		return retList;
	}	
}
