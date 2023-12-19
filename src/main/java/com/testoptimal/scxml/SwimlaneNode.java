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

package com.testoptimal.scxml;

import java.util.List;
import java.util.Map;



public class SwimlaneNode {
	private transient boolean readOnly=false;

	private String uid = "";
	private String typeCode = "swimlane";

	private String name = "Swimlane Name";
	private String orient = "horizontal";
	private String color;
	private String textColor;
	private String notepad;
	
	private Map<String,Integer> position = new java.util.HashMap<String,Integer>();

	public void setName(String name_p) {
		this.name = name_p;
	}
	public String getName() {
		return this.name;
	}

	public void setOrientation(String orient_p) {
		this.orient = orient_p;
	}
	public String getOrientation() {
		return this.orient;
	}

	public List<Lane> lanes = new java.util.ArrayList<Lane>();

	/**
	 * 
	 * returns a list of states that are within the boundary of the specified lane.
	 * @param scxml_p
	 * @param laneNum_p
	 * @return
	 */
	public List<StateNode> getStateList (ScxmlNode scxml_p, int laneNum_p) {
		List<StateNode> retList = new java.util.ArrayList<StateNode>();
		int laneStart = this.getLaneStartPos(laneNum_p);
		int laneEnd = laneStart + this.getLaneSize(laneNum_p);
		
		for (StateNode state: scxml_p.getChildrenStates()) {
			if (this.isStateInRange(state, laneStart,laneEnd)) {
				retList.add(state);
			}
		}
		return retList;
	}

	private int getLaneSize(int laneNum_p) {
		if (laneNum_p>=this.lanes.size()) return -1;
		return this.lanes.get(laneNum_p).size;
	}
	
	public boolean isVertical () {
		return this.orient.equalsIgnoreCase("Vertical");
	}

	public boolean isHorizontall () {
		return this.orient.equalsIgnoreCase("Horizontal");
	}
	
	private boolean isStateInRange (StateNode stateNode_p, int startPos_p, int endPos_p) {
		int stateStartPos = 0;
		int stateEndPos = 0;
		if (this.isHorizontall()) {
			stateStartPos = stateNode_p.getTop();
			stateEndPos = stateStartPos + stateNode_p.getHeight();
		}
		else {
			stateStartPos = stateNode_p.getLeft();
			stateEndPos = stateStartPos + stateNode_p.getWidth();
		}
		
		if (startPos_p <= stateStartPos && endPos_p >= stateEndPos) return true;
		else return false;
	}

	
	private int getLaneStartPos (int laneNum_p) {
		int cumPos = 0;
		for (int idx=0; idx<laneNum_p; idx++) {
			cumPos += this.lanes.get(idx).size;
		}
		return cumPos + (this.orient.equalsIgnoreCase("horizontal")?this.position.get("top"):this.position.get("left"));
	}
	
	/**
	 * returns the lane index (number) 0-base for the given lane label.
	 * @param laneLabel_p 0-based index if found, -1 if not found.
	 * @return
	 */
	public int findLaneNum (String laneLabel_p) {
		for (int idx=0; idx< this.lanes.size(); idx++) {
			if (this.lanes.get(idx).name.trim().equalsIgnoreCase(laneLabel_p)) {
				return idx;
			}
		}
		return -1;
	}
	
	public List<Lane> getLanes () {
		return this.lanes;
	}
	
	public class Lane {
		public String name;
		public int size;
		public String color;
		public String textColor;
	}
}


