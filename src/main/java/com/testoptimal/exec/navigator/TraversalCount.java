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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;

public class TraversalCount {
	private AtomicInteger travCount = new AtomicInteger();
	//Keyed on UID
	private Map<String, Integer> travExpCountMap = new java.util.HashMap<>();
	private Map<String, AtomicInteger> travCountMap = new java.util.HashMap<>();
	
	public TraversalCount (Map<String, Integer> travExpCountMap_p) {
		this.travExpCountMap = travExpCountMap_p;
	}
	
	public void reset() {
		this.travCountMap.clear();
		this.travCount.set(0);
	}
	
	public boolean addTravState (String stateUID_p) {
		this.addTrav(stateUID_p);
		return true;
	}

	public boolean addTravTrans (Transition trans_p) {
		if (trans_p.isFake()) {
			trans_p = trans_p.getForTrans();
		}
		if (trans_p==null) {
			return false;
		}
		else {
			String uid = trans_p.getTransNode().getUID();
			if (this.travExpCountMap.containsKey(uid)) {
				this.addTrav(uid);
				return true;
			}
			else return false;
		}
	}
	
	public boolean addTravTag (String tag_p) {
		this.addTrav(tag_p);
		return true;
	}
	
	private void addTrav (String travUID_p) {
		this.travCount.incrementAndGet();
		AtomicInteger ai = this.travCountMap.get(travUID_p);
		if (ai==null) {
			this.travCountMap.put(travUID_p, new AtomicInteger(1));
		}
		else ai.incrementAndGet();
	}
	
	public void addTravList (List<Transition> travList_p) {
		for (Transition trav: travList_p) {
			this.addTravTrans(trav);
		}
	}
	
	public void addTravPath (SequencePath path_p) {
		this.addTravList(path_p.getTransList());
	}

	public int getTravCount () {
		return this.travCount.get();
	}
	public int getTravCount (String travUID_p) {
		AtomicInteger ai = this.travCountMap.get(travUID_p);
		return ai==null?0:ai.intValue();
	}
	public int getTravCount (Transition trans_p) {
		if (trans_p.isFake()) {
			trans_p = trans_p.getForTrans();
		}
		if (trans_p==null) {
			return 0;
		}
		else {
			return this.getTravCount(trans_p.getTransNode().getUID());
		}
	}
	
	public int getTravDistinctCount () {
		return this.travCountMap.size();
	}
	
	/**
	 * returns number of traversal still left to be covered. negative number
	 * to indicate the traversals exceeded the required traversal count.
	 * @param trav_p
	 * @return
	 */
	public int getTravCountLeft (String travUID_p) {
		Integer ei = this.travExpCountMap.get(travUID_p);
		if (ei==null) ei = 0;
		AtomicInteger ai = this.travCountMap.get(travUID_p);
		if (ai==null) return ei;
		return ei - ai.intValue();
	}

	public int getTravCountLeft (Transition trans_p) {
		if (trans_p.isFake()) {
			trans_p = trans_p.getForTrans();
		}
		if (trans_p==null) {
			return 0;
		}
		else {
			return this.getTravCountLeft(trans_p.getTransNode().getUID());
		}
	}
	
	public boolean isRequired (String travUID_p) {
		Integer ei = this.travExpCountMap.get(travUID_p);
		return ei!=null && ei > 0;
	}
	public boolean isRequired (Transition trans_p) {
		if (trans_p.isFake()) {
			trans_p = trans_p.getForTrans();
		}
		if (trans_p==null) {
			return false;
		}
		else {
			return this.isRequired(trans_p.getTransNode().getUID());
		}
	}
	
	public int getTravDistinctCountExpected () {
		return this.travExpCountMap.size();
	}
	
	public int getCoveragePct () {
		if (this.travExpCountMap.isEmpty()) return 0;
		int compCnt = 0;
		for (Map.Entry<String, Integer> entry: this.travExpCountMap.entrySet()) {
			Integer exp = entry.getValue();
			int tCount = this.getTravCount(entry.getKey());
			if (tCount >= exp) {
				compCnt++;
			}
		}
		return (int)Math.round(100.0 * compCnt / this.travExpCountMap.size());
	}
}
