package com.testoptimal.exec.FSM;

import java.util.List;

import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.util.StringUtil;

import openOptima.graph.Edge;
import openOptima.network.Node;

@IGNORE_INHERITED_METHOD
public class State extends Node {

	private static int nextI=1;
	public static final String LoopBackTransID = "_finalLoopback";
	public static final String FinalExitTransID = "_finalExit";
	public static final String InitialEntryTransID = "_initEntry";
	public static final String FinalExitStateID = "_exit";
	private java.util.ArrayList <State> childStates = new java.util.ArrayList<State>();	
	private boolean fake = false;
	
	public boolean isFake() {
		if (this.fake) return true;
		return this.isPseudoVertex();
	}
	
	public void setFake(boolean fake_p) { this.fake = fake_p; }
	
	// state should be a real state or for a real state. Therefore, this.stateNode should not be null.
	private State forState = null;
	public State getForState() {
		return this.forState;
	}
	public void setForState(State state) {
		this.forState = state;
		this.stateNode = state.getStateNode();
	}

	/**
	 * submodel state that contains this state.
	 */
	private State subModelState;
	public void setSubModelState(State subModelState_p) {
		this.subModelState = subModelState_p;
	}
	public State getSubModelState() {
		return this.subModelState;
	}
	
	
	/**
	 * returns true if aState_p is a final state within this submodel state, considering
	 * the final states in sub-states of the super state in this submodel are not submodel final states.
	 */
	public boolean isSubModelFinal() {
		if (!this.isFinal()) return false;
		
		if (this.getStateNode().getParentStateNode() == null) {
			return true;
		}
		return false;
	}

	public Transition getLoopBackTrans() {
		return this.getTransition(State.LoopBackTransID);
	}
	
	public void reset(ModelMgr modelMgr_p) {
		// nothing to do right now.
	}
	
		
	private State exitState;
	
	private StateNode stateNode;
	public StateNode getStateNode() { return this.stateNode; }
	public void setStateNode(StateNode stateNode_p) { this.stateNode = stateNode_p; }
	
	
	public State (String stateId, int stateType) {
		super(nextI++, stateId);
		switch (stateType) {
			case initialVertex:
			case simpleVertex:
			case superVertex:
			case pseudoVertex:
			case finalVertex:
				this.setVertexType(stateType);
				break;
			default:
				this.setVertexType(simpleVertex);
		}
	}

	public int addChildState (State childObj) {
		if (this.childStates==null) this.childStates = new java.util.ArrayList <State>(5);
		this.childStates.add(childObj);
		return this.childStates.size();
	}
	
	public String getStateId () { return this.getMarker(); }
	
	public int getNodeI () { return this.getId(); }

	/**
	 * returns the child states.  Pseudo exit state is not included.
	 * empty arrayList if this state is a simple state.
	 * @return ArrayList of all children states.
	 */
	public java.util.ArrayList <State> getChildStates() { 
		java.util.ArrayList <State>returnList = new java.util.ArrayList <State>();
		if (this.childStates==null) return returnList;
		
		for (int i=0; i<this.childStates.size(); i++) {
			State tempObj = (State) this.childStates.get(i);
			if (tempObj!=this.exitState) returnList.add(tempObj);
		}
		return returnList;
		
	}
	
	/**
	 * returns the list of substates of current state that have the 
	 * isInitial set to true.
	 */
	public java.util.ArrayList <State> getInitialStateList() {
		java.util.ArrayList <State>retList = new java.util.ArrayList<State>();
		for (int i=0; i<this.childStates.size(); i++) {
			State stateObj = (State) this.childStates.get(i);
			if (stateObj.isInitial()) retList.add(stateObj);
		}
		return retList;
	}

	/**
	 * returns true if the stateObj_p passed in is one of the initial child state(s) of this state.
	 * Note in SCXML standard, one super state can only have one initial substate.
	 * In webmbt, this constraint has been loosen to allow multiple entry points 
	 * in the web application testing.
	 * @param stateObj_p
	 * @return True if the state passed in is an initial children state of this state.
	 */
	public boolean isInitialState (State stateObj_p) {
		if (stateObj_p==null) return false;
		for (int i=0; i<this.childStates.size(); i++) {
			State stateObj = (State) this.childStates.get(i);
			if (stateObj.isInitial() && stateObj==stateObj_p) return true;
		}
		return false;
	}

	/**
	 * returns true if this state has the initial attribute set to true.
	 * returns true if this state is fake state.
	 * @return True if this state is an initial state of its parent state.
	 */
	public boolean isInitial() {
		if (this.stateNode==null) return false;
		return this.stateNode.getIsInitial();
	}

	/**
	 * returns true if this state is the initial state for the model. Note submodel may have its own
	 * initial state which is not considered as the main model's initial state.
	 * @return
	 */
	public boolean isModelInitial() {
		if(this.isModelInitFinalState() && this.stateNode.getIsInitial()) {
			return true;
		}
		else return false;
	}

	public boolean isModelFinal() {
		if(this.isModelInitFinalState() && this.stateNode.getIsFinal()) {
			return true;
		}
		else return false;
	}

	public boolean isModelInitFinalState() {
		if (!this.isInitial() && !this.isFinal()) return false;
		if (this.stateNode.isReadOnly()) {
			return false;
		}
		
		if (this.stateNode.getParentStateNode() == null) return true;
		else return false;
	}

	public boolean isSubModelInitial() {
		if (this.isInitial() && this.getStateNode().getParentStateNode() == null) {
			return true;
		}
		else return false;
	}
	
	public void setExitState (State exitState_p) {
		this.exitState = exitState_p;
	}
	
	public State getExitState() { 
		return this.exitState;
	}
	
	/**
	 * returns true if this state is an exit state of this super state.
	 * @param stateObj_p
	 * @return true if stateObj_p is a children of this state and is a final (exit) state.
	 */
	public boolean isExitState (State stateObj_p) {
		if (stateObj_p==null) return false;
		if (stateObj_p==this.exitState) return true;
		else return false;
	}
	
	/**
	 * returns a list of child states of this state that have isFinal attribute set to true.
	 * @return ArrayList of all children states that are final states.
	 */
	public java.util.ArrayList <State> getFinalStateList () {
		java.util.ArrayList <State> returnList = new java.util.ArrayList<State>();
		for (int i=0; i<this.childStates.size(); i++) {
			State tempObj = (State) this.childStates.get(i);
			if (tempObj.isFinal()) returnList.add(tempObj);
		}
		return returnList;
		
	}

	/**
	 * returns true if this state has the isFinal set to true.
	 * returns true for fake state.
	 * @return true if this state is a final state.
	 */
	public boolean isFinal() {
		if (this.stateNode==null) return false;
		return this.stateNode.getIsFinal();
	}
	
	public String toString() {
		StringBuffer retBuf = new StringBuffer("State ");
		
		retBuf.append(this.getMarker()).append("\n");
		
		java.util.ArrayList <Edge> list = this.getEdgesOut ();
		for (int j=0; j<list.size(); j++) {
			Transition trans = (Transition) list.get(j);
			retBuf.append("\t\t").append(trans.toString()).append("\n");
		}

		return retBuf.toString();
	}

	/**
	 * returns the transition object for event id passed in.
	 * @param transMarker_p marke of the transition/event
	 * @return TransitionArc object, null if not found
	 */
	public Transition getTransition (String transMarker_p) {
		java.util.ArrayList <Edge> transList = this.getEdgesOut();
		Transition transObj = null;
		for (int i=0; i<transList.size(); i++) {
			transObj = (Transition) transList.get(i);
			if (transMarker_p.equalsIgnoreCase(transObj.getMarker())) return transObj;
		}
		return null;
	}
	
	public Transition getTransByUID (String uid_p) {
		if (StringUtil.isEmpty(uid_p)) return null;
		java.util.ArrayList <Edge> transList = this.getEdgesOut();
		for (int i=0; i<transList.size(); i++) {
			Transition transObj = (Transition) transList.get(i);
			if (uid_p.equalsIgnoreCase(transObj.getTransNode().getUID())) return transObj;
		}
		return null;
	}

	public Transition findSubModelEntryTrans() {
		if (!this.getStateNode().isSubModelState()) return null;
		for (Transition trans: this.getAllTrans(false)) {
			if (trans.isFake()) {
				State initState = (State) trans.getToNode();
				if (initState.isInitial()) return trans;
			}
		}
		return null;
	}
		

	/**
	 * returns all transitions including transitions in chlidren states if traverseChildrenState_p passed in is true.
	 */
	public List <Transition> getAllTrans(boolean traverseChildrenState_p) {
		List <Transition> retList = new java.util.ArrayList<Transition>();
		for (Edge edge: this.getEdgesOut()) {
			retList.add((Transition) edge);
		}
		if (traverseChildrenState_p) {
			for (State childState: this.getChildStates()) {
				retList.addAll(childState.getAllTrans(true));
			}
		}
		
		return retList;
	}
	
	/**
	 * returns the total trav count for all incoming transitions.
	 */
	public int sumIncomingTransTravCount() {
		List<Edge> inArcList = this.getArcsInto();
		int cnt = 0;
		for (int i=0; i<inArcList.size(); i++) {
			Edge edgeObj = (Edge) inArcList.get(i);
			if (((Transition) edgeObj).isFake()) continue;
			cnt += edgeObj.getMinTraverseCount();
		}
		return cnt;
	}
	

	public List<Transition> getAllIncomingTrans() {
		List<Transition> retList = new java.util.ArrayList<Transition>();
		List<Edge> inArcList = this.getArcsInto();
		for (int i=0; i<inArcList.size(); i++) {
			Edge edgeObj = (Edge) inArcList.get(i);
			if (((Transition) edgeObj).isFake()) continue;
			retList.add((Transition) edgeObj);
		}
		
		return retList;
	}
	
	/**
	 * returns the count of real transition that requires at least one traversal.
	 * 
	 * @return
	 */
	public int getReqTransCount() {
		int transCount = 0;
		for (Edge edge: this.getEdgesOut()) {
			Transition trans = (Transition) edge;
			if (trans.isFake()) continue;
			if (trans.getMinTraverseCount()>0) {
				transCount++;
			}
		}
		
		return transCount;
	}

	public void setActive(boolean active_p, boolean recursive_p) {
		this.setActive (active_p);
		for (Transition trans: this.getAllTrans(false)) {
			trans.setActive(active_p);
		}
		if (recursive_p) {
			for (State state: this.getChildStates()) {
				if (state.isFake() || !state.isSuperVertex()) {
					continue;
				}
				state.setActive(active_p, true);
			}
		 }
	}
	
	public List<Transition> getTransByFinalStateUID (String uid_p) {
		List<Transition> retList = new java.util.ArrayList<>();
		for (Transition trans: this.getAllTrans(false)) {
			if (!trans.isFake() && trans.getTransNode().matchFinalStateUID(uid_p)) {
				retList.add(trans);
			}
		}
		return retList;
	}
}
