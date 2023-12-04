package com.testoptimal.scxml;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.plugin.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.plugin.MScriptInterface.NOT_MSCRIPT_METHOD;
import com.testoptimal.util.StringUtil;

@IGNORE_INHERITED_METHOD
public class StateNode implements Comparable<StateNode> {
	public static enum ActivateType {TRAV_COUNT, TRANS_COUNT, WEIGHT, VAR};
	public static enum FiringType {ALL, RANDOM, VAR};
	public static enum NodeType {INITIAL, FINAL, STATE, BRANCH, SWITCH, SYNCBARH, SYNCBARV};

	private boolean readOnly=false;
	@JsonIgnore
	private transient StateNode parentStateNode;
	@JsonIgnore
	private transient ScxmlNode scxmlNode;
	
	/**
	 * only used for saving model to restore submodel states after saving.
	 */
	@JsonIgnore
	private transient List<StateNode> subModelStates;

	private String typeCode = "state";
	private String desc = ""; 
	private String uid = "";

	private String notepad = "";
	private String stateID = "";
	private boolean isFinal = false;
	private boolean isInitial = false;
	private NodeType nodeType = NodeType.STATE;

	private String color = "";
	private String textColor = "";
	
	private String stereotype = "";

	/**
	 * name of the model to be attatched to this state.
	 */
	private String subModel = "";
	
	private boolean hideSubstates = false;
	
	/**
	 * The max number of millis allowed for the process action, if exceeded will cause the state traversal to be tallied 
	 * in the number of traversal over the limit.
	 */
	private int maxMillis = Integer.MAX_VALUE;
	
	/**
	 * type of the node: 
	 * <ul>
	 * <li>FSM (default, null, blank) - normal, FSM state, behaves like XOR 1, IOR 1
	 * <li>AND (all, no purging) - all incoming trans required to fire state, only consume 1 traversal
	 * from each incoming transitions. Behaves like XOR m of m transitions
	 * <li>XOR (n/m, purged on duplicates) - must have n distinct transition traversals of
	 * any of the transitions. consume 1 traversal from each of the n transitions.
	 * <li>IOR (n/m) - must have n transition traversals, duplicate traversal on transitions ok and counted
	 * </ul>
	 * 
	 * <ul>
	 * 	<li>TraversalCount: count of traversals regardless of transitions, duplicate traversals of same
	 * transitions are counted. This is the default if not specified to be compatible with FSM.
	 * <li>TransitionCount: count of distinctive transitions that have traversals, one transition only
	 * counted once regardless of multiple of traversals. 
	 * <li>Weight: sum of weight of all traversals must be equal or exceeds the activateThreshold
	 * </ul>
	 * @since 4.0
	 */
	private String activateType = "";
	
	/**
	 * quantity for activateType.
	 */
	private int activateThreshold = 1; // <=0 is treated as 1.

	/**
	 * type of firing rule to use.
	 * <ul>
	 * 	<li>ALL - fire all transitions if no guard condition or guard condition evaluates to true
	 *  <li>RANDOM - randomly select one transitions from the trans weight that has no guard condition
	 *  or guard condition evaluates to true.
	 * </ul>
	 */
	private String firingType = "RANDOM";
	
	//	public StateNode initialChildState;
	private List<StateNode> childrenStates = new java.util.ArrayList<StateNode>();
	private List <TransitionNode> transitions = new java.util.ArrayList<TransitionNode>();

	private Map<String, Float> position;
	private Map<String, String> css = new java.util.HashMap<>();

	/**
	 * The max number of millis allowed for the process action, if exceeded will cause the state traversal to be tallied 
	 * in the number of traversal over the limit.
	 */
	public int getMaxMillis() { return this.maxMillis; }
	
	public String getStateID() { 
		if (this.stateID==null) return null;
		return this.stateID.trim(); 
	}

	
	
	public String getActivateType() { 
//		if (Util.isEmpty(this.activateType)) this.activateType = "XOR";
		return this.activateType; 
	}

	public ActivateType getActivateTypeEnum() {
		return resolveActivateType(this.activateType);
	}

	public static ActivateType resolveActivateType(String activateType_p) {
		if (StringUtil.isEmpty(activateType_p)) return ActivateType.TRAV_COUNT;
		else if (activateType_p.equalsIgnoreCase("TRANS_COUNT")) return ActivateType.TRANS_COUNT;
		else if (activateType_p.equalsIgnoreCase("WEIGHT")) return ActivateType.WEIGHT;
		else if (activateType_p.equalsIgnoreCase("VAR")) return ActivateType.VAR;
		else return ActivateType.TRAV_COUNT;
		
//		if (Util.isEmpty(this.activateType)) return ActivateType.FSM;
//		else if (this.activateType.equalsIgnoreCase("XOR")) return ActivateType.XOR;
//		else if (this.activateType.equalsIgnoreCase("IOR")) return ActivateType.IOR;
//		else if (this.activateType.equalsIgnoreCase("AND")) return ActivateType.AND;
//		else return ActivateType.FSM;
	}
	
	public static FiringType resolveFiringType(String firingType_p) {
		if (StringUtil.isEmpty(firingType_p)) return FiringType.RANDOM;
		else if (firingType_p.equalsIgnoreCase("ALL")) return FiringType.ALL;
		else if (firingType_p.equalsIgnoreCase("VAR")) return FiringType.VAR;
		else return FiringType.RANDOM;
	}
	
	public String getFiringType() { 
		return this.firingType; 
	}

	public FiringType getFiringTypeEnum() {
		return resolveFiringType(this.firingType);
	}
	
	public int getActivateThreshold() {
		if (this.activateThreshold<=0) this.activateThreshold = 1;
		return this.activateThreshold;
	}
	
	
	public String getStereotype () {
		if (StringUtil.isEmpty(this.stereotype)) {
			if (this.isInitial) {
				return "i";
			}
			else if (this.isFinal) {
				return "f";
			}
		}
		return this.stereotype;
	}
	
	public boolean hasStereotype(String stereotype_p) {
		if (this.stereotype==null) return false;
		return this.stereotype.equalsIgnoreCase(stereotype_p);
	}
	
	public boolean isDummy() {
		return this.hasStereotype("D");
	}
	
	public String getTextColor() { 
		if (this.textColor==null || this.textColor.equals("#")) this.textColor = "";
		return this.textColor; 
	}


	
	public boolean getIsFinal() { return this.isFinal; }
	public boolean getIsInitial() { return this.isInitial; }
	
	public void setIsFinal(boolean isFinal_p) {
		this.isFinal = isFinal_p;
	}

	public void setIsInitial(boolean isInitial_p) {
		this.isInitial = isInitial_p;
	}

	private int getPosAttr(String attr_p) { 
		if (this.position==null) return 0;
		Float ret = this.position.get(attr_p);
		if (ret==null) return 0;
		else return Math.round(ret); 
	}
	
	public int getLeft() {
		return this.getPosAttr("left");
	}
	public int getTop() { 
		return this.getPosAttr("top");
	}
	public int getWidth() {
		return this.getPosAttr("width");
	}
	public int getHeight() { 
		return this.getPosAttr("height");
	}

	public List<StateNode> getChildrenStates() { return this.childrenStates; }
	public List <TransitionNode> getTransitions() { return this.transitions; }

	public boolean isSuperState() { 
		if (this.childrenStates==null) return false;
		if (this.childrenStates.size()<=0) return false;
		else return true;
	}

	/**
	 * finds the transition with the given event_p value.
	 * @param event_p
	 * @return TransitionNode for the event_p passed in. Null if not found.
	 */
	public TransitionNode findTrans(String event_p) {
		if (event_p==null || this.transitions==null) return null;
		for (int i=0; i<this.transitions.size(); i++) {
			if (this.transitions.get(i).getEvent().equalsIgnoreCase(event_p))
				return this.transitions.get(i);
		}
		return null;
	}

	/**
	 * finds a transition to the target nodee
	 * @param targetNode_p
	 * @return
	 */
	public TransitionNode findTrans(StateNode targetNode_p) {
		for (TransitionNode trans: this.transitions) {
			if (trans.getTargetNode()==targetNode_p) {
				return trans;
			}
		}
		return null;
	}
	
	
	/**
	 * finds a state with the given state id/marker.  It traverses through all its children.
	 * @return StateNode or null if not found
	 */
	public StateNode findState(String stateMarker_p) {
		for (int i=0; i<this.childrenStates.size(); i++) {
			StateNode stateObj = (StateNode) this.childrenStates.get(i);
			if (stateObj.stateID.equalsIgnoreCase(stateMarker_p)) return stateObj;
			StateNode tempStateObj = stateObj.findState(stateMarker_p);
			if (tempStateObj!=null) return tempStateObj;
		}
		return null;
	}
	
	/**
	 * finds a state with the given state uid.  It traverses through all its children.
	 * @return StateNode or null if not found
	 */
	public StateNode findStateByUID(String uid_p) {
		for (int i=0; i<this.childrenStates.size(); i++) {
			StateNode stateObj = (StateNode) this.childrenStates.get(i);
			if (stateObj.getUID().equals(uid_p)) return stateObj;
			StateNode tempStateObj = stateObj.findStateByUID(uid_p);
			if (tempStateObj!=null) return tempStateObj;
		}
		return null;
	}
	
	/**
	 * returns the number of transitions including all transitions of its sub-substates, etc.
	 * @return
	 */
	public int getTransCount() {
		int transCount = 0;
		for (TransitionNode trans: this.transitions) {
			if (trans.getTraverseTimes()>=0) {
				transCount++;
			}
		}
		for (int i=0; i<this.childrenStates.size(); i++) {
			StateNode checkState = (StateNode) this.childrenStates.get(i);
			transCount += checkState.getTransCount();
		}
		return transCount;
	}

	/**
	 * returns the number of required transitions (transitions with traverse count at least 1) including all transitions of its sub-substates, etc.
	 * @return
	 */
	public int getReqTransCount() {
		int transCount = 0;
		for (TransitionNode trans: this.transitions) {
			if (trans.getTraverseTimes()>0) {
				transCount++;
			}
		}
		
		for (int i=0; i<this.childrenStates.size(); i++) {
			StateNode checkState = (StateNode) this.childrenStates.get(i);
			transCount += checkState.getReqTransCount();
		}
		return transCount;
	}

	/**
	 * returns the transition for the uid_p passed in.
	 */
	public TransitionNode findTransByUID(String uid_p) {
		for (TransitionNode transObj: this.transitions) {
			if (transObj.getUID().equals(uid_p)) return transObj;
		}
		
		for (StateNode stateObj: this.childrenStates) {
			TransitionNode transObj = stateObj.findTransByUID(uid_p);
			if (transObj!=null) return transObj;
		}
		return null;
	}

	/**
	 * sets the submodel name. It register and unregister submodel references accordingly.
	 * return true if submodel property has been changed.  The caller should reload the model if submodel property
	 * is changed.
	 */
	public boolean setSubModelName(String subModelName_p, String modelName_p) throws Exception {
		String oldSubModel = this.subModel;
		this.subModel = subModelName_p;
		if (StringUtil.isEmpty(oldSubModel)) oldSubModel = "";
		if (StringUtil.isEmpty(subModelName_p)) subModelName_p = "";
		if (this.subModel.equalsIgnoreCase(oldSubModel)) return false;
		
		if (StringUtil.isEmpty(this.subModel)) {
			if (!StringUtil.isEmpty(this.stereotype) && this.stereotype.equalsIgnoreCase("M")) {
				this.stereotype = "";
			}
		}
		else {
			this.stereotype = "M";
		}
		return true; // changed
	}
	
	/**
	 * returns the submodel name.
	 */
	public String getSubModel() { return this.subModel; }

	
	/**
	 * returns true if this state is a submodel state.
	 */
	public boolean isSubModelState() {
		if (StringUtil.isEmpty(this.subModel)) return false;
		else return true;
		
	}
	
	/**
	 * sets the readOnly attribute for the model and all of its children objects (states and trans)
	 */
	public void setReadOnly(boolean readOnly_p) {
		this.readOnly = readOnly_p;
		List<StateNode> tempList = this.getChildrenStates();
		for (int i=0; i<tempList.size(); i++) {
			StateNode checkState = (StateNode) tempList.get(i);
			checkState.setReadOnly(readOnly_p);
		}
		
		for (int i=0; i<this.transitions.size(); i++) {
			TransitionNode transObj = (TransitionNode) this.transitions.get(i);
			transObj.setReadOnly(readOnly_p);
		}
		return;
	}
	
	public void addToColorList(Map<String, String> colorList_p) {
		if (!this.noNeedToCover()) {
			colorList_p.put(this.uid, this.color);
		}
		for (StateNode stateObj: this.getChildrenStates()) {
			if (stateObj.noNeedToCover()) {
				continue;
			}
			stateObj.addToColorList(colorList_p);
		}
		
		for (TransitionNode transObj:this.transitions) {
			if (transObj.noNeedToCover()) continue;
			String tColor = transObj.getCSS ("color");
			colorList_p.put(transObj.getUID(), tColor);
		}
	}

	public List<TransitionNode> getAllTrans() {
		List<TransitionNode> retList = new java.util.ArrayList<TransitionNode>();
		retList.addAll(this.transitions);
		for (StateNode state: this.childrenStates) {
			retList.addAll(state.getAllTrans());
		}
		return retList;
	}
	
	/**
	 * recursively sum up all states
	 * @return
	 */
	public List<StateNode> getAllStates() {
		List<StateNode> retList = new java.util.ArrayList<StateNode>();
		for (StateNode state: this.childrenStates) {
			retList.add(state);
			retList.addAll(state.getAllStates());
		}
		return retList;
	}
	

	@Override
	public int compareTo(StateNode node_p) {
		String cpID = "";
		if (!StringUtil.isEmpty(node_p.stateID)) {
			cpID = node_p.stateID.trim().toLowerCase();
		}
		
		String thisID = "";
		if (!StringUtil.isEmpty(this.stateID)) {
			thisID = this.stateID.trim().toLowerCase();
		}
		int i = thisID.compareTo(cpID);
		return i;
	}


	public void sortChildStates () {
		Collections.sort(this.getChildrenStates());
		for (StateNode stateNode: this.getChildrenStates()) {
			Collections.sort(this.transitions);
			stateNode.sortChildStates();
		}
	}

	public boolean isBranchNode () {
		if (this.stereotype!=null && this.stereotype.equalsIgnoreCase("B")) {
			return true;
		}
		else return false;
	}

	public boolean isSyncBar () {
		if (this.stereotype!=null && (this.stereotype.equalsIgnoreCase("BH") || this.stereotype.equalsIgnoreCase("BV"))) {
			return true;
		}
		else return false;
	}

	public boolean containsBranchNode () {
		if (this.isBranchNode()) return true;
		for (StateNode child: this.childrenStates) {
			if (child.containsBranchNode()) return true;
		}
		return false;
	}
	
	/**
	 * returns the <a href="https://en.wikipedia.org/wiki/Cyclomatic_complexity">cyclomatic complexity</a> for this state including sub-states.
	 * 
	 * @param mainOnly_p
	 * @return
	 */
	public int getComplexity (boolean mainOnly_p) {
		int retCC = 0;
		List<TransitionNode> transList = this.getTransitions();
		if (!transList.isEmpty()) {
			retCC += this.getTransitions().size() - 1;
		}
		for (StateNode child: this.childrenStates) {
			if (child.isSubModelState() && mainOnly_p) continue;
			retCC += child.getComplexity(mainOnly_p);
		}
		return retCC;
	}
	
	public List<TransitionNode> findTransByStereotype(String stereotype_p, boolean searchChildState_p) {
		List<TransitionNode> retList = new java.util.ArrayList<TransitionNode>();
		for (TransitionNode checkTrans: this.getTransitions()) {
			if (checkTrans.hasStereotype(stereotype_p)) {
				retList.add(checkTrans);
			}
		}
		
		if (searchChildState_p && this.childrenStates!=null) {
			for (StateNode cState: this.childrenStates) {
				retList.addAll(cState.findTransByStereotype(stereotype_p, searchChildState_p));
			}
		}
		
		return retList;
	}

	/**
	 * returns true if this state has a transition to the targetState_p passed in.
	 * 
	 * @param targetState_p
	 * @return
	 */
	public boolean hasTransTo (StateNode targetState_p) {
		for (TransitionNode checkTrans: this.getTransitions()) {
			if (checkTrans.getTargetNode()==targetState_p) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public boolean noNeedToCover () {
		if (this.hasStereotype("V")) {
			return true;
		}
		else return false;
	}
	
	/**
	 * returns the model name or submodel name of this state.
	 */
	public String modelName() {
		if (this.parentStateNode==null) {
			return this.scxmlNode.getModelName();
		}
		
		StateNode node = this;
		while (true) {
			node = node.getParentStateNode();
			if (node==null) return null;
			if (node.isSubModelState()) {
				return node.getSubModel();
			}
		}
	}
	

	public String getUID() {
		return this.uid;
	}
	
	/**
	 * <p>INTERNAL USE ONLY</p>
	 */
	@NOT_MSCRIPT_METHOD
	public void setParentStateNode (StateNode parentState_p, ScxmlNode scxmlNode_p) {
		this.parentStateNode = parentState_p;
		this.scxmlNode = scxmlNode_p;
		
		for (int i=this.childrenStates.size()-1; i>=0; i--) {
			StateNode s = this.childrenStates.get(i);
			if (s == null) {
				this.childrenStates.remove(i);
			}
		}
		for (int j=this.transitions.size()-1; j>=0; j--) {
			TransitionNode t = this.transitions.get(j);
			if (t==null) {
				this.transitions.remove(j);
			}
		}

		for (StateNode x: this.childrenStates) {
			x.setParentStateNode(this, scxmlNode_p);
		}
		for (TransitionNode x: this.transitions) {
			x.setParentStateNode(this);
		}
	}

	public StateNode getParentStateNode() {
		return this.parentStateNode;
	}
	
	public ScxmlNode getScxmlNode() {
		return this.scxmlNode;
	}
	
	public boolean isReadOnly() {
		return this.readOnly;
	}

	public String getDesc() {
		return this.desc;
	}
	
	public void setStateIDandUID (String stateID_p, String uid_p) {
		this.stateID = stateID_p;
		this.uid = uid_p;
	}
	
	public void setPosition (Map<String, Float> posMap_p) {
		this.position = posMap_p;
	}
}
