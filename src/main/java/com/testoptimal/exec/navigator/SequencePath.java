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

package com.testoptimal.exec.navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.util.StringUtil;

public class SequencePath {
	private List<Transition> transList = new java.util.ArrayList<Transition>();
	private String pathId;
	private int reqTransCnt = 0;
	private String pathDesc = "";
	private int curTransIdxInPath = -1;

	
	public void setPathDesc (String desc_p) {
		this.pathDesc = desc_p;
	}
	
	public void reset() {
		this.curTransIdxInPath = -1;
	}
	
	public Transition nextTrans() {
		this.curTransIdxInPath++;
		if (this.curTransIdxInPath >= this.transList.size()) {
			return null;
		}
		return this.transList.get(this.curTransIdxInPath);
	}

	public boolean isStartingPath () {
		return this.curTransIdxInPath==-1;
	}
	public boolean isEndingPath() {
		return this.curTransIdxInPath == this.transList.size() - 1;
	}
	
	public List<State> getStateList() {
		return this.transList.stream().map(t -> (State)t.getToNode()).toList();
	}

	public SequencePath(List<Transition> transList_p) {
		this.transList = new ArrayList<>(transList_p.size());
		this.transList.addAll(transList_p);
		Transition lastTrans = this.transList.get(this.transList.size()-1);
		if (lastTrans.isLoopbackTrans()) {
			this.transList.remove(lastTrans);
		}
		this.pathId = this.transList.stream().map(t -> String.valueOf(t.getIntId())).collect(Collectors.joining("^"));
		this.reqTransCnt = 0;
		for (Transition trans: this.transList) {
			if (trans.getMinTraverseCount() > 0) {
				this.reqTransCnt++;
			}
		}
	}

	public int getReqTransCnt() {
		return this.reqTransCnt;
	}
	
	public List<Transition> getTransList() {
		return this.transList;
	}

	public int findTransIdx(int targetNodeID_p, int nthOccurence_p) {
		int foundCnt = 0;
		for (int i = 0; i < this.transList.size(); i++) {
			if (this.transList.get(i).getToNode().getId() == targetNodeID_p) {
				if (foundCnt == nthOccurence_p) {
					return i;
				}
				foundCnt++;
			}
		}
		return -1;
	}

	public String getPathId() {
		return this.pathId;
	}
	
	public void setPathId(String newPathID_p) {
		this.pathId = newPathID_p;
	}

	public String getPathDesc() {
		if (StringUtil.isEmpty(this.pathDesc)) {
			return this.pathId;
		}
		else return this.pathDesc;
	}
	
	public String toString() {
		StringBuffer retBuf = new StringBuffer();
		retBuf.append(this.pathDesc);
		for (Transition trans: this.transList) {
			if (trans.isFake()) continue;
			retBuf.append(": ").append(trans.getEventId()).append("=> ").append(trans.getToNode().getMarker());
		}
		retBuf.append("[seq: ").append(this.pathId).append(", ID: ")
			.append(this.pathId);
		retBuf.append("]");
		return retBuf.toString();
	}
	
	public boolean containsTrans(Transition transObj_p) {
		return this.containsTrans((Transition) transObj_p);
	}
	
	public Transition getTransAt(int idx_p) {
		if (idx_p<0 || idx_p>=this.transList.size()) {
			return null;
		}
		else return this.transList.get(idx_p);
	}
	
	public int getLength() {
		return this.transList.size();
	}
	
	public Transition addAltRoute (List<Transition> altTransList_p) {
		this.transList.addAll(this.curTransIdxInPath, altTransList_p.stream().filter(t-> t.getTransNode()!=null).toList());
		for (Transition trans: altTransList_p) {
			if (trans.getMinTraverseCount() > 0) {
				this.reqTransCnt++;
			}
		}
		return this.transList.get(this.curTransIdxInPath);
	}
	
	public static List<SequencePath> breakupToPaths(List<Transition> transList_p) {
		List<SequencePath> retList = new java.util.ArrayList<>();
		List<Transition> aTransList = new java.util.ArrayList<>();
		for (Transition trans: transList_p) {
			aTransList.add(trans);
			if (((State) trans.getToNode()).isModelFinal()) {
				if (aTransList.size() > 1) {
					SequencePath path = new SequencePath(aTransList);
					path.setPathDesc("Path " + retList.size());
					retList.add(path);
				}
				aTransList = new java.util.ArrayList<Transition>();				
			}
		}
		if (!aTransList.isEmpty()) {
			// keep last in-complete path
			SequencePath path = new SequencePath(aTransList);
			path.setPathDesc("Path " + retList.size());
			retList.add(path);
		}
		return retList;
	}
	
}
