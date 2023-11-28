package com.testoptimal.graphing;

import java.util.List;
import java.util.Map;

import com.testoptimal.exec.FSM.ModelMgr;
import com.testoptimal.exec.FSM.StateNetwork;
import com.testoptimal.scxml.StateNode;
import com.testoptimal.scxml.TransitionNode;
import com.testoptimal.util.StringUtil;

import openOptima.graph.Edge;
import openOptima.graph.Vertex;

public class GraphTrans {
	public static final float wrapRatioNode = 2.5f;
	public static final float wrapRatioEdge = 4.0f;
	public static final String newLineString = "\\n";

	
	private GraphState fromState;
	private GraphState toState;
	private String transLabel;
	private String transArrow;
	
	public GraphTrans (GraphState fromState_p, GraphState toState_p,
			String transLabel_p, String transArrow_p) throws Exception {
		this.fromState = fromState_p;
		this.toState = toState_p;
		this.transArrow = transArrow_p;
		this.transLabel = transLabel_p;
		if (this.fromState==null) {
			throw new Exception ("Unknown fromState: " + fromState_p);
		}
		if (this.toState==null) {
			throw new Exception ("Unknown toState: " + toState_p);
		}
	}
	
	public GraphTrans (GraphState rootState_p, String fromState_p, String toState_p,
			String transLabel_p, String transArrow_p) throws Exception {
		this.fromState = rootState_p.findGraphState(fromState_p);
		this.toState = rootState_p.findGraphState(toState_p);
		this.transArrow = transArrow_p;
		this.transLabel = transLabel_p;
		
		if (this.fromState==null) {
			throw new Exception ("Unknown fromState: " + fromState_p);
		}
		if (this.toState==null) {
			throw new Exception ("Unknown toState: " + toState_p);
		}
	}
	
	public GraphState getFromState() { 
		return this.fromState;
	}

	public GraphState getToState() { 
		return this.toState;
	}
	
	public String getTransLabel() {
		return this.transLabel;
	}

	public String getTransArrow() {
		return (this.transArrow==null?"":this.transArrow);
	}
	
	/**
	 * 
	 * @param transList_p
	 * @param rootState_p
	 * @param colorList_p key on uid or seq# (0-base).
	 * @return
	 * @throws Exception
	 */
	public static List<GraphTrans> genTransList (ModelMgr modelMgr_p, List<TransitionNode> transList_p, 
			GraphState rootState_p, Map<String, String> colorList_p) throws Exception {
		if (colorList_p==null) colorList_p = new java.util.HashMap<String, String>();
		List<GraphTrans> retList = new java.util.ArrayList<GraphTrans>();
		int i=0;
		for (TransitionNode trans: transList_p) {
			if (trans==null) continue;
			
			StateNode fromStateNode = trans.getParentStateNode();
			String fromNodeID = fromStateNode.getStateID();
//			if (!Util.isTrue(fromStateNode.getHideSubModel())) {
//				String newFrom = trans.getSubmodeFinalState();
//				if (!Util.isEmpty(newFrom)) fromNodeID = newFrom;
//			}
			GraphState fromState = rootState_p.findGraphStateByAsID(fromStateNode.getUID());
			if (fromState==null && fromStateNode.getParentStateNode()!=null) {
				fromState = rootState_p.findGraphStateByAsID(fromStateNode.getParentStateNode().getUID());
			}
			if (fromState==null) {
				continue;
			}
			
			String toNodeID = trans.getTargetNode().getStateID();
			GraphState toState = rootState_p.findGraphStateByAsID(trans.getTargetNode().getUID());
			if (toState==null) {
				continue;
			}

			String transLabel = trans.getDisplayEvent();
//			String transFlags = trans.getTransDisplayFlags(modelMgr_p, ",");
//			if (!StringUtil.isEmpty(transFlags)) {
//				transLabel = transLabel + " " + transFlags;
//			}
// \r, \n, \l caused <color><size> tag to be broker
//			transLabel = StringUtil.wrapText(transLabel, wrapRatioEdge, "\\r");
			
			String transArrow = "";
			String transColor = colorList_p.get(trans.getUID());
			if (transColor==null) {
				transColor = colorList_p.get(String.valueOf(i));
			}
			if (transColor==null || transColor.equalsIgnoreCase("darkgreen")) {
				transColor = colorList_p.get("*");
			}
			if (!StringUtil.isEmpty(transColor)) {
				if (!transColor.startsWith("#")) transColor = "#" + transColor;
				transLabel = "<size:12><color:" + transColor + ">" + transLabel + "</color></size>";
				transArrow = "[" + transColor + ",bold]";
			}
			if (StringUtil.isEmpty(transLabel)) transLabel = trans.getUID();
			GraphTrans gTrans = new GraphTrans(fromState, toState, 
					transLabel, transArrow);
			retList.add(gTrans);
			i++;
		}
		return retList;
	}
	
	public static List<GraphTrans> genTransList (StateNetwork networkObj_p, 
			GraphState rootState_p) throws Exception {
		List<GraphTrans> retList = new java.util.ArrayList<GraphTrans>();
		for (Object edge: networkObj_p.getArcList(true)) {
			Edge trans = (Edge) edge;
			Vertex fromStateNode = trans.getFromVertex();
			String fromNodeID = fromStateNode.getMarker();
			GraphState fromState = rootState_p.findGraphState(fromNodeID);
			
			String toNodeID = trans.getToVertex().getMarker();
			GraphState toState = rootState_p.findGraphState(toNodeID);

			String transLabel = trans.getMarker() + " (" + trans.getMinTraverseCount() + ")";
			transLabel = StringUtil.wrapText(transLabel, wrapRatioEdge, "\\n");
			String transArrow = "";
			GraphTrans gTrans = new GraphTrans(fromState, toState, 
					transLabel, transArrow);
			retList.add(gTrans);
		}
		return retList;
	}
	
}
