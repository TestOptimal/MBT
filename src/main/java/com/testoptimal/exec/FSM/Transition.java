package com.testoptimal.exec.FSM;

import java.util.List;

import com.testoptimal.exec.mscript.MbtScriptExecutor;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.StringUtil;

import openOptima.network.postman.PostmanArc;

@IGNORE_INHERITED_METHOD
public class Transition extends PostmanArc {
	private static final int transCost = 5;
	private static final int fakeTransCost = 1;

	private TransitionNode transNode;
	public TransitionNode getTransNode() { return this.transNode; }
	public void setTransNode(TransitionNode transNode_p) { 
		this.transNode = transNode_p; 
		this.restoreDist();
	}
	
//	private Map<String, String> traversalVarList = new java.util.HashMap<String, String>();
	
	
	// transition can be a fake trans not for any real trans. Therefore this.transNode can be null
	private Transition forTrans = null;
	public Transition getForTrans() {
		return this.forTrans;
	}
	public void setForTrans(Transition trans) {
		this.forTrans = trans;
		this.transNode = trans.getTransNode();
	}
	
	// only used at runtime to allow user to override during execution.
	private transient int minTravTimes = -1;
	
	@Override
	public int getMinTraverseCount() {
		if (this.minTravTimes>=0) return this.minTravTimes;
		else return super.getMinTraverseCount();
	}
	public void setMinTraverseCountRT(int newMinTimes_p) {
		this.minTravTimes = newMinTimes_p;
	}
	
	private transient int maxTravTimes = Integer.MAX_VALUE;
	public int getMaxTraverseCount() {
		return this.maxTravTimes;
	}
	
	// sets the max traver count allowed at runtime different from StateNode
	public void setMaxTraverseCountRT(int newMaxTimes_p) {
		this.maxTravTimes = newMaxTimes_p;
	}
	
	private transient TransitionNode proxyToTrans;
	public void setProxyToTrans(TransitionNode trans_p) {
		this.proxyToTrans = trans_p;
	}
	public TransitionNode getProxToTrans() {
		return this.proxyToTrans;
	}
	
	/**
	 * number of times this transition has been traversed. should reset this to 0 before model execution.
	 */
	private int traversedCount = 0;
	
	/**
	 * resets the number of times this transition is traversed to 0.
	 *
	 */
	public void reset(ModelMgr modelMgr_p) throws Exception { 
		this.minTravTimes = -1;
		this.maxTravTimes = Integer.MAX_VALUE;
		this.traversedCount = 0;
	}
	
	/**
	 * returns the event id of this transition.  This is the "event" tag defined in scxml.
	 * @return Event ID
	 */
	public String getEventId () { return this.getMarker(); }
	
	/**
	 * returns the target state id.  This is the "target" tag defined in scxml.
	 * @return Target state id of this transition
	 */
	public String getTargetStateId () { return this.getToNode().getMarker(); }
	
	public String getFromStateId () { return this.getFromNode().getMarker(); }

	public int getActualTraverseTimes() { return this.getCount(); }
	
	public double getTraverseCost() { return this.getDist(); }
	public double getActualTraverseCosts() { return this.getCount() * this.getDist(); }

	public void setTraverseRequired () { this.setMinMaxCount(1, Integer.MAX_VALUE); }

	public boolean isTraverseRequired() { return this.getMinTraverseCount()>0; }
	
	public void setTraverseOptional () { this.setMinMaxCount(0, Integer.MAX_VALUE); }
	 
	public boolean isTraverseOptional() { return this.getMinTraverseCount() <= 0; }

//	public String getTraversalVarValue(String varName_p) {
//		return this.traversalVarList.get(varName_p);
//	}
//	
//	public void setTraversalVar(String varName_p, String value_p) {
//		if (value_p==null) this.traversalVarList.remove(varName_p);
//		this.traversalVarList.put(varName_p, value_p);
//	}
//	
//	public void clearTraversalVars() {
//		this.traversalVarList.clear();
//	}
//	
//	public void addTraversalVars(Map<String, String> traversalVarList_p) {
//		this.traversalVarList.putAll(traversalVarList_p);
//	}
//
//	public Map<String, String> getTraversalVarList() {
//		return this.traversalVarList;
//	}

	public Transition (State fromStateObj, State toStateObj, String eventId, int triggerTimes) {
		super (fromStateObj, toStateObj, 1, true);
		setTransitionArc (eventId, triggerTimes, false);
	}
	
	public Transition (State fromStateObj, State toStateObj, String eventId, int triggerTimes, boolean fakeTrans) {
		super (fromStateObj, toStateObj, (fakeTrans?fakeTransCost:transCost), true);
		setTransitionArc (eventId, triggerTimes, fakeTrans);
	}

	private void setTransitionArc (String eventId, int triggerTimes, boolean fakeTrans) {
		this.setMinMaxCount(triggerTimes, Integer.MAX_VALUE);
		this.setMarker(eventId);
		if (fakeTrans) {
			this.setFlag("fake", "Y");
		}
	}

	public String toString() {
		StringBuffer retBuf = new StringBuffer("Transition ");
		retBuf.append(this.getMarker())
			  .append("; source=").append(this.getFromStateId()).append("; target=").append(this.getToVertex().getMarker());
		return retBuf.toString();
	}
	
	public int getMaxMillis() { return this.transNode.getMaxMillis(); }
	
	public boolean isReadOnly() {
		if (this.transNode==null) return false;
		
		return this.transNode.isReadOnly();
	}
	
	
	public int incrementTraversedCount() {
		return this.traversedCount++;
	}
	
	public int getTraversedCount() {
		return this.traversedCount;
	}
	
//	private static final String GuardHintCode = "guardHint";
//	private static final String SatisfyingHintCode = "satifyingHint";
//	public String getGuardHintThread() {
//		if (this.transNode==null) return null;
//		return this.getExecAttr(GuardHintCode, this.transNode.getGuardHint());
//	}
//	public void setGuardHintThread(String value_p) {
//		this.setExecAttr(GuardHintCode, value_p);
//	}
//
//	public String getSatisfyingHintThread() {
//		if (this.transNode==null) return null;
//		return this.getExecAttr(SatisfyingHintCode, this.transNode.getSatisfyingHint());
//	}
//	public void setSatisfyingHintThread(String value_p) {
//		this.setExecAttr(SatisfyingHintCode, value_p);
//	}

//	private transient java.util.HashMap<String, String> execAttrList;
//	public void setExecAttr(String attrName_p, String value_p) {
//		this.execAttrList.put(attrName_p, value_p);
//	}
//	public String getExecAttr(String attrName_p, String valueIfNotFound_p) {
//		String threadVal = this.execAttrList.get(attrName_p);
//		if (threadVal==null) {
//			if (valueIfNotFound_p!=null) {
//				this.execAttrList.put(attrName_p, valueIfNotFound_p);
//				threadVal = valueIfNotFound_p;
//			}
//		}
//		return threadVal;
//	}
	
	
	/**
	 * returns true if this transition "set" defined satisfies the transition passed in.
	 * Assumes that trans_p is not null and has a legal guard condition set.
	 * @param trans_p transition that has guard condition
	 * @return true if this transition sets the condition that satisfies transition passed in. 
	 * false on any error,exception.
	 */
	public boolean satisfyGuard (Transition forTrans_p, MbtScriptExecutor scriptExec_p) {
		TransitionNode forTransNode = forTrans_p.getTransNode();

		// fake trans, trans with guards and trans with no satisfying hint set don't qualify
		if (this.transNode==null || !StringUtil.isEmpty(this.transNode.getGuard())) return false;
		if (StringUtil.isEmpty(this.transNode.getSatisfyingHint())) return false;
		
		String [] transHintList = ArrayUtil.splitString(this.transNode.getSatisfyingHint(), ",;");
		String [] reqHintList = ArrayUtil.splitString(forTransNode.getGuardHint(), ",;");
		List<String> matchHintList = ArrayUtil.findOverlap(transHintList, reqHintList);
		return !matchHintList.isEmpty();
	}
	
	
	/**
	 * returns true if this transition is a fake transition.
	 * @return True if current transition is a fake transition.
	 */
	public boolean isFake () { 
		return this.hasFlag("fake"); 
	}
	
	public boolean isSubModelExitTrans () {
		State fromNodeSubModel = ((State) this.getFromNode()).getSubModelState();
		State toNodeSubModel = ((State) this.getToNode()).getSubModelState();
		if (fromNodeSubModel==null && toNodeSubModel==null ||
			fromNodeSubModel == toNodeSubModel) {
			return true;
		}
		else return false;
	}

	public boolean isLoopbackTrans () {
		return this.getMarker().endsWith(State.LoopBackTransID);
	}
	
//	public boolean isFinalExitTrans () {
//		return this.getMarker().endsWith(State.FinalExitTransID);
//	}
	
//	public boolean isLoopBackTrans() {
//		boolean ret =  this.getMarker().equalsIgnoreCase(State.LoopBackTransID);
//		return ret;
//	}
	
//	public boolean isInitialEntryTrans() {
//		boolean ret = this.getMarker().equalsIgnoreCase(State.InitialEntryTransID);
//		return ret;
//	}
	
	public double restoreDist () {
		if (this.transNode!=null) {
			this.setDist(this.transNode.getWeight());
			this.setFirstDist(this.transNode.getWeight());
		}
		return this.getDist();
	}
	
	public double getDist0() {
		double d = this.getDist();
		return d;
	}
}
