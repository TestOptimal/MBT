/***********************************************************************************************
 * Copyright (c) 2009-2024 TestOptimal.com
 *
 * This file is part of TestOptimal MBT.
 *
 * TestOptimal MBT is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * TestOptimal MBT is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See 
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with TestOptimal MBT. 
 * If not, see <https://www.gnu.org/licenses/>.
 ***********************************************************************************************/

package com.testoptimal.graphing;

import java.util.List;

import com.testoptimal.scxml.StateNode;
import com.testoptimal.util.StringUtil;

import openOptima.graph.Vertex;
import openOptima.network.postman.PostmanNetwork;

public class GraphState {
	private List<GraphState> childStateList = new java.util.ArrayList<GraphState>();
	private String stateLabel;
	private String stateDesc;
	private String stateExtra;
	private String stateAsID;
	private boolean initialState = false;
	private boolean finalState = false;
	
	public GraphState(String stateLabel_p, String stateDesc_p,
			String stateExtra, String stateAsiD) {
		this.stateLabel = stateLabel_p;
		this.stateDesc = stateDesc_p;
		this.stateExtra = stateExtra;
		this.stateAsID = stateAsiD;
		if (StringUtil.isEmpty(this.stateLabel)) this.stateLabel = this.stateAsID;
	}
	
	public void addChildState (GraphState child_p) {
		this.childStateList.add(child_p);
	}
	
	public String getStateLabel () {
		return this.stateLabel;
	}
	
	public String getStateDesc () {
		return this.stateDesc;
	}

	public String getStateExtra () {
		return this.stateExtra;
	}

	public String getStateAsID () {
		return this.stateAsID;
	}

	public List<GraphState> getChildStateList () {
		return this.childStateList;
	}
	
	public void setInitialState() {
		this.initialState = true;
	}
	
	public void setFinalState() {
		this.finalState = true;
	}
	
	public boolean isInitialState() {
		return this.initialState;
	}

	public boolean isFinalState() {
		return this.finalState;
	}

	/**
	 * returns the state As ID defined for the given child state label.
	 * 
	 * @param stateLabel_p
	 * @return null if child state is not found or no state as id is defined.
	 */
	public String getChildStateAsID (String stateLabel_p) {
		String foundStateAsID = null;
		for (GraphState state: this.childStateList) {
			if (state.stateLabel.equals(stateLabel_p)) {
				foundStateAsID = state.stateAsID;
				break;
			}
			
			if (state.childStateList.isEmpty()) continue;
			foundStateAsID = state.getChildStateAsID(stateLabel_p);
			if (foundStateAsID!=null) {
				break;
			}
		}
		return foundStateAsID;
	}
	
	public GraphState findGraphState (String stateLabel_p) {
		GraphState foundState = null;
		for (GraphState state: this.childStateList) {
			if (state.stateLabel.equals(stateLabel_p)) {
				foundState = state;
				break;
			}
			
			if (state.childStateList.isEmpty()) continue;
			foundState = state.findGraphState(stateLabel_p);
			if (foundState!=null) {
				break;
			}
		}
		return foundState;
	}
	

	public GraphState findGraphStateByAsID (String stateAsID_p) {
		GraphState foundState = null;
		for (GraphState state: this.childStateList) {
			if (state.stateAsID!=null && state.stateAsID.equals(stateAsID_p)) {
				foundState = state;
				break;
			}
			
			if (state.childStateList.isEmpty()) continue;
			foundState = state.findGraphStateByAsID(stateAsID_p);
			if (foundState!=null) {
				break;
			}
		}
		return foundState;
	}
	public boolean hasChildStates() {
		return !this.childStateList.isEmpty();
	}
	

	/**
	 * populate this graph state with child states passed in.  It
	 * nest calls child states to populate grand child states.
	 * @param childStateList_p
	 */
	public void addChildState(List<StateNode> childStateList_p) {
		for (StateNode state: childStateList_p) {
			GraphState gState = new GraphState (state.getStateID(), null, null, state.getUID());
			if (state.getIsInitial()) {
				gState.setInitialState();
			}
			
			if (state.getIsFinal()) {
				gState.setFinalState();
			}
			this.addChildState(gState);
			
			if (state.isSuperState()) {
//				if (!state.isSubModelState() || !Util.isTrue(state.getHideSubModel())) {
					gState.addChildState(state.getChildrenStates());
//				}
			}
		}
		return;
	}
	

	/**
	 * populate this graph state with child states passed in.  It
	 * nest calls child states to populate grand child states.
	 * @param childStateList_p
	 */
	public void addChildState(PostmanNetwork networkObj_p) {
		for (Vertex state: networkObj_p.getVertexList(true)) {
			GraphState gState = new GraphState (state.getMarker(), null, null, String.valueOf(state.getId()));
			if (state.getVertexType()==Vertex.initialVertex) {
				gState.setInitialState();
			}
			
//			if (state.getIsFinal()) {
//				gState.setFinalState();
//			}
			this.addChildState(gState);
			
		}
		return;
	}
}
