package com.testoptimal.exec.navigator;

import java.util.ArrayList;
import java.util.List;

import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.exec.exception.MBTException;
import com.testoptimal.exec.navigator.SequencePath;

import openOptima.NoSolutionException;

public class PathBuilder {
	private StateNetwork networkObj;
	private List<Transition> transList = new ArrayList<>();
	private State atState;
	
	public PathBuilder (StateNetwork networkObj_p) {
		this.networkObj = networkObj_p;
		this.atState = networkObj_p.getHomeState();
	}
	
	public PathBuilder navigateTo(State toState_p) throws MBTException {
		List<Transition> tlist = this.networkObj.findShortestPath(this.atState.getId(), toState_p.getId());
		this.transList.addAll(tlist.stream().filter(t -> !t.isLoopbackTrans()).toList());
		this.atState = toState_p;
		return this;
	}
	public PathBuilder navigateTo(String toUID_p) throws MBTException {
		State toState = this.networkObj.findStateByUID(toUID_p);
		if (toState!=null) {
			this.navigateTo(toState);
		}
		else {
			Transition t = this.networkObj.findTransByUID(toUID_p);
			this.transList.add(t);
			this.navigateTo((State) t.getToNode());
		}
		return this;
	}
	
	public PathBuilder addTrans(Transition trans_p) {
		this.transList.add(trans_p);
		return this;
	}
	
	public SequencePath completePath(boolean toFinal_p) throws MBTException {
		if (toFinal_p && !this.atState.isModelFinal()) {
			State targetState = this.networkObj.getHomeState();
			this.navigateTo(targetState);
			this.atState = targetState;
		}
		return new SequencePath(this.transList);
	}
	
	public static List<SequencePath> breakUpIntoPaths(List<Transition> transList_p) {
		List<SequencePath> retList = new java.util.ArrayList<>();
		List<Transition> aTransList = new java.util.ArrayList<>();
		transList_p.stream()
			.filter(t -> t.getTransNode()!=null)
			.forEach(t -> {
				aTransList.add(t);
				if (((State) t.getToNode()).isModelFinal()) {
					if (aTransList.size() > 1) {
						SequencePath path = new SequencePath(aTransList);
						path.setPathDesc("Path " + retList.size());
						retList.add(path);
					}
					aTransList.clear();				
				}
			});
		if (!aTransList.isEmpty()) {
			// keep last in-complete path
			SequencePath path = new SequencePath(aTransList);
			path.setPathDesc("Path " + retList.size());
			retList.add(path);
		}
		return retList;
	}
}
