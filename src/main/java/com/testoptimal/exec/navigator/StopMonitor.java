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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.testoptimal.exec.ExecutionSetting;
import com.testoptimal.exec.FSM.State;
import com.testoptimal.exec.FSM.Transition;
import com.testoptimal.scxml.StopCondition;
import com.testoptimal.server.model.Requirement;

public class StopMonitor {
	private TraversalCount transCoverage;
	private TraversalCount stateCoverage;
	private TraversalCount reqmntCoverage;
	private long startedMillis;
	private boolean stopAtFinalOnly;
	
	private State homeState;
	private int totalPathCount;
	private AtomicInteger execPathCount = new AtomicInteger(0);
	
//	private StopCondition stopCond;
	private int stopTransCoverage;
	private int stopTransCount;
	private int stopReqCoverage;
	private int stopHomeRunCount;
	private int stopElapseMillis;
	private ExecutionSetting execSetting;
	
	
	public StopMonitor (ExecutionSetting execSetting_p, Navigator nav_p) throws Exception {
		this.execSetting = execSetting_p;
		this.stopAtFinalOnly = execSetting_p.isStopAtFinalOnly();
		
		Map<String, Integer> reqmntMap = new java.util.HashMap<>();
		for (Requirement tag: execSetting_p.getModelMgr().getRequirementList()) {
			reqmntMap.put(tag.name, 1);
		}
		this.reqmntCoverage = new TraversalCount(reqmntMap);
		
		StopCondition stopCond = this.execSetting.getStopCondition();
		this.stopTransCoverage = stopCond.getTransCoverage()==null?0: stopCond.getTransCoverage();
		this.stopTransCount = stopCond.getTransCount()==null?0: stopCond.getTransCount();
		this.stopReqCoverage = stopCond.getReqCoverage()==null?0: stopCond.getReqCoverage();
		this.stopHomeRunCount = stopCond.getHomeRunCount()==null?0: stopCond.getHomeRunCount();
		Integer intObj = (Integer) this.execSetting.getOption("maxPaths");
		if (intObj!=null && intObj > 0) {
			this.stopHomeRunCount = intObj;
		}
		this.stopElapseMillis = stopCond.getElapseTime()==null?0: stopCond.getElapseTime()*60000;
	}
	
	public void start(Sequencer sequencer_p) {
		this.startedMillis = System.currentTimeMillis();
		this.homeState = sequencer_p.getNetworkObj().getHomeState();
		this.totalPathCount = sequencer_p.getPathCount();

		Map<String, Integer> transReqMap = new java.util.HashMap<>();
		for (Transition trans: sequencer_p.getNetworkObj().getAllRequiredTrans()) {
			transReqMap.put(trans.getTransNode().getUID(), trans.getMinTraverseCount());
		}
		
		this.transCoverage = new TraversalCount(transReqMap);
		this.stateCoverage = new TraversalCount(new java.util.HashMap<>());		
	}
	
	public int getProgressPercent () {
		double completeRate = 0, rate;
		boolean atLeastOneCond = false;
		if (this.stopElapseMillis > 0) {
			rate = 1.0 * (System.currentTimeMillis() - this.startedMillis) / this.stopElapseMillis;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopHomeRunCount > 0 && this.stateCoverage!=null) {
			rate = 1.0 * this.stateCoverage.getTravCount(this.homeState.getStateNode().getUID()) / this.stopHomeRunCount;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopTransCount > 0 && this.transCoverage!=null) {
			rate = 1.0 * this.transCoverage.getTravCount() / this.stopTransCount;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopReqCoverage > 0 && this.reqmntCoverage!=null) {
			rate = 1.0 * this.reqmntCoverage.getCoveragePct() / this.stopReqCoverage;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		if (this.stopTransCoverage > 0 && this.transCoverage!=null) {
			rate = 1.0 * this.transCoverage.getCoveragePct() / this.stopTransCoverage;
			completeRate = Math.max(completeRate, rate);
			atLeastOneCond = true;
		}
		// default to 100% trans coverage if it's not one of the path sequencer. 
		// path based sequencers stops when it runs out of paths
		if (!atLeastOneCond && this.totalPathCount > 0) {
			rate = 1.0 * this.execPathCount.get() / this.totalPathCount ;
			completeRate = Math.max(completeRate, rate);
		}
		
		if (!atLeastOneCond) {
			// default to trans coverage
			rate = 1.0 * this.transCoverage.getCoveragePct() / 100;
			completeRate = Math.max(completeRate, rate);
		}
		return (int) Math.floor(completeRate * 100);
	}
	
	public void setTotalPathCount (int count_p) {
		this.totalPathCount = count_p;
	}
	
	/**
	 * returns true if the execution can continue, false if the execution should stop/abort.
	 * @return
	 */
	public boolean checkIfContinue (boolean atFinalState_p) {
		if (this.getProgressPercent() >= 100) {
			if (this.stopAtFinalOnly && !atFinalState_p) {
				return true;
			}
			else return false;
		}
		else return true;
	}

	public TraversalCount getStateCoverage() {
		return this.stateCoverage;
	}
	
	public TraversalCount getTransCoverage() {
		return this.transCoverage;
	}
	
	public int addOneExecPath() {
		return this.execPathCount.incrementAndGet();
	}
	
	public int getMacPaths() {
		return this.stopHomeRunCount;
	}
}
