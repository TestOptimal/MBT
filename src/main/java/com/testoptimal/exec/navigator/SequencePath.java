package com.testoptimal.exec.navigator;

import java.util.List;

import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.util.ArrayUtil;
import com.testoptimal.util.StringUtil;

public class SequencePath {
	private List<Transition> transList = new java.util.ArrayList<Transition>();
	private String pathId;
	private List<State> pathStateList;
	private List<String> pathTransIdList;
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

	public SequencePath(List<Transition> transList_p) {
		this.transList = transList_p;
		Transition lastTrans = this.transList.get(this.transList.size()-1);
		if (lastTrans.isLoopbackTrans()) {
			this.transList.remove(lastTrans);
		}
		this.genPathId();
		this.reqTransCnt = 0;
		for (Transition trans: this.transList) {
			if (trans.getMinTraverseCount() > 0) {
				this.reqTransCnt++;
			}
		}
	}

	public String genPathId () {
		this.pathTransIdList = new java.util.ArrayList<>(this.transList.size());
		this.pathStateList = new java.util.ArrayList<>(this.transList.size());
		for (Transition trans: this.transList) {
			this.pathTransIdList.add(String.valueOf(trans.getIntId()));
			this.pathStateList.add((State)trans.getToNode());
		}
		this.pathId = ArrayUtil.join(this.pathTransIdList, "^");
		return this.pathId;
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
	
	public List<State> getPathStateList () {
		return this.pathStateList;
	}
	
	public int getLength() {
		return this.transList.size();
	}
	
	public Transition addAltRoute (List<Transition> altTransList_p) {
		this.transList.addAll(this.curTransIdxInPath, altTransList_p);
		this.genPathId();
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
			if (trans.isLoopbackTrans()) {
				if (aTransList.size() > 1) {
					SequencePath path = new SequencePath(aTransList);
					path.setPathDesc("Path " + retList.size());
					retList.add(path);
				}
				aTransList = new java.util.ArrayList<Transition>();
			}
			else {
				aTransList.add(trans);
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
