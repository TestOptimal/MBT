package com.testoptimal.scxml;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testoptimal.exec.mscript.MScriptInterface.IGNORE_INHERITED_METHOD;
import com.testoptimal.util.StringUtil;

@IGNORE_INHERITED_METHOD
public class TransitionNode implements Comparable<TransitionNode> {
	private boolean readOnly=false;
	
	@JsonIgnore
	private transient StateNode parentStateNode;
	@JsonIgnore
	private transient StateNode targetNode;

	private String typeCode = "transition";
	private String desc = "";
	private String uid = "";

	private String notepad = "";

	private String targetUID = "";
	private String event = "";
	private int weight = 5;
	private int traverseTimes = 1;

//	private String color = "";
//	private String textColor = "";
	private boolean hideName = false;

	private PosInfo posInfo;
	private Map<String,String> css = new java.util.HashMap<String,String>();
	
	// used for graphing (determing shapes and styles)
	private String stereotype = "";

	/**
	 * guard condition formatted: varname=value.
	 */
	private String guard = "";
	
	/**
	 * contains the list of UID of transitions that can resolve the guard condition for this transition
	 */
	private String guardResolvers = "";

	/**
	 * The max number of millis allowed for the event action, if exceeded will cause the transition traversal to be tallied 
	 * in the number of traversal over the limit.
	 */
	private int maxMillis = Integer.MAX_VALUE;
	
	private String subModelFinalStateUID = "";

	public boolean matchFinalStateUID (String UID_p) {
		return StringUtil.isEmpty(this.subModelFinalStateUID) || this.subModelFinalStateUID.equals(UID_p);
	}
	
	public String getStereotype () {
		return this.stereotype;
	}
	
	public String getTargetUID() {
		return this.targetUID;
	}
	
	public boolean hasStereotype(String stereotype_p) {
		if (this.stereotype==null) return false;
		return this.stereotype.equalsIgnoreCase(stereotype_p);
	}

	public boolean isDummy() {
		return this.hasStereotype("D");
	}

	public String getEvent() { return this.event;}
	public void setEvent(String event_p) { 
		String newEvent = StringUtil.removeInvalidXMLChar(event_p); 
		if (!StringUtil.isEmpty(newEvent)) {
			this.event = newEvent;
		}
	}

	public StateNode getTargetNode() { return this.targetNode; }
	
	public void setTargetNode(StateNode targetNode_p) {
		this.targetNode = targetNode_p;
		if (this.targetNode!=null) {
			this.targetUID = this.targetNode.getUID();
		}
	}
	
	public int getTraverseTimes() { return this.traverseTimes; }

	public int getWeight() { return this.weight; }
	
	/**
	 * The max number of millis allowed for the event action, if exceeded will cause the transition traversal to be tallied 
	 * in the number of traversal over the limit.
	 */
	public int getMaxMillis() {
		return this.maxMillis; 
	}
	
	public String getGuard() {
		return this.guard;
	}
		
	/**
	 * returns true if this trans has guard - either guard condition or guard triggers.
	 * @param transObj_p
	 * @return
	 * @throws Exception
	 */
	public boolean hasGuard() {
		return !StringUtil.isEmpty(this.guard);
	}

	@Override
	public int compareTo(TransitionNode node_p) {
		String cpID = "";
		if (!StringUtil.isEmpty(node_p.event)) {
			cpID = node_p.event.trim().toLowerCase();
		}
		
		String thisID = "";
		if (!StringUtil.isEmpty(this.event)) {
			thisID = this.event.trim().toLowerCase();
		}
		int i = thisID.compareTo(cpID);
		return i;
	}

	public boolean isNameMatch(String expr_p) {
		return StringUtil.matchSearch(this.event, expr_p);
	}

	public String getDisplayEvent() {
		return this.hideName?null: this.event;
	}
	
	public String getGuardResolvers() {
		return this.guardResolvers==null?"":this.guardResolvers;
	}
	
	public boolean isGuardResolver(String uid_p) {
		return this.guardResolvers!=null && this.guardResolvers.indexOf(uid_p)>=0;
	}
	
	public boolean noNeedToCover() {
		if (this.hasStereotype("V") || this.traverseTimes==0) return true;
		else return false;
	}
	
	public String modelName() {
		StateNode stateNode = this.parentStateNode;
		String modelName = stateNode.modelName();
		return modelName;
	}
	
	public StateNode getParentStateNode() { 
		return this.parentStateNode; 
	}

	public String getUID() {
		return this.uid;
	}
	
	public void setUID (String uid_p) {
		this.uid = uid_p;
	}
	
	public void setReadOnly(boolean readOnly_p) {
		this.readOnly = readOnly_p;
	}

	public String getDesc() {
		return this.desc;
	}

	public boolean isReadOnly() {
		return this.readOnly;
	}
	
	public void setParentStateNode (StateNode stateNode_p) {
		this.parentStateNode = stateNode_p;
	}
	
	public String getCSS (String cssAttr_p) {
		return this.css.get(cssAttr_p);
	}
	
	public class PosInfo {
		private String startDir;
		private String endDir;
		private List<Map<String, Float>> posList = new java.util.ArrayList<Map<String,Float>>();
		private Map<String, Integer> label = new java.util.HashMap<String,Integer>();
	}
	
	public boolean isHideName () {
		return this.hideName;
	}
	
}


